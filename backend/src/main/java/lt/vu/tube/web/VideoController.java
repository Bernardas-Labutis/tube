package lt.vu.tube.web;

import lt.vu.tube.config.VideoConfig;
import lt.vu.tube.model.LambdaResponse;
import lt.vu.tube.model.MediaTypeResponseBody;
import lt.vu.tube.repository.VideoRepository;
import lt.vu.tube.entity.Video;
import lt.vu.tube.enums.VideoStatusEnum;
import lt.vu.tube.response.VideoUploadResponse;
import lt.vu.tube.util.AWSCloudFrontUtils;
import lt.vu.tube.util.AWSLambdaUtils;
import lt.vu.tube.util.AWSS3Utils;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.util.Streams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(value = "/video")
public class VideoController {

    @Autowired
    private AWSS3Utils s3Utils;

    @Autowired
    private AWSCloudFrontUtils cloudFrontUtils;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private AWSLambdaUtils lambdaUtils;

    @Autowired
    VideoConfig videoConfig;

    private static final Logger logger = Logger.getLogger(VideoController.class.toString());

    @RequestMapping(value = "/upload")
    //@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PreAuthorize("hasAuthority('user:read')")
    public VideoUploadResponse uploadVideo(HttpServletRequest request) throws Exception {
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart) {
            return VideoUploadResponse.fail("Request must be multipart");
        }
        Set<String> neededParams = new HashSet<>(Set.of("fileName", "fileSize", "file"));


        String fileName = "";
        int reportedFileSize;

        Video video = null;

        ServletFileUpload upload = new ServletFileUpload();
        FileItemIterator iterStream = upload.getItemIterator(request);
        //Getting multipart request parts
        while (iterStream.hasNext()) {
            FileItemStream item = iterStream.next();
            InputStream stream = item.openStream();

            if (item.isFormField()) {
                if (neededParams.contains(item.getFieldName())) {
                    neededParams.remove(item.getFieldName());
                    String value = Streams.asString(stream);
                    switch (item.getFieldName()) {
                        case "fileName":
                            fileName = value;
                            break;
                        case "fileSize":
                            reportedFileSize = Integer.parseInt(value);
                            break;
                    }
                }
            }
            else if(neededParams.contains(item.getFieldName())) {
                if (neededParams.size() == 1) {
                    neededParams.remove(item.getFieldName());
                }
                else {
                    return VideoUploadResponse.fail("There are missing parameters or they are in wrong order");
                }
                //TODO: add checks for file size when user is created

                //No transaction because the operation is long and we might need to check the status midway from a different call

                //Create video object to get the path we're going to use
                video = new Video();
                video.setFileName(fileName);
                video = videoRepository.save(video);

                //Start upload
                video.setPath("videos/" + video.getId().toString());
                video.setMime(item.getHeaders().getHeader("content-type")); //Later update this after upload by extracting metadata
                var response = s3Utils.createMultipartUpload(video.getPath(), item.getHeaders().getHeader("content-type"));
                video.setStatus(VideoStatusEnum.UPLOADING);
                video = videoRepository.save(video);

                try {
                    long fileLength = 0L;
                    byte[] bytes;
                    do {
                        bytes = stream.readNBytes(1024 * 1024 * 10);
                        if (bytes.length != 0) {
                            fileLength += bytes.length;
                            s3Utils.uploadPart(response.uploadId(), bytes);
                        }
                    } while (bytes.length != 0);
                    //Finish upload
                    s3Utils.completeMultipartUpload(response.uploadId());
                    video.setFileSize(fileLength);
                    video.setStatus(VideoStatusEnum.PROCESSING);
                    video = videoRepository.save(video);
                    LambdaResponse<MediaTypeResponseBody> mediaTypeResponse;
                    try {
                        mediaTypeResponse = lambdaUtils.getMediaType(video.getPath());
                    }
                    catch (Exception exception) {
                        logger.log(Level.SEVERE, "getMediaType failed with exception: " + exception.getMessage(), exception);
                        s3Utils.deleteFile(video.getPath());
                        video.setStatus(VideoStatusEnum.INVALID);
                        video = videoRepository.save(video);
                        return VideoUploadResponse.fail("Failed to process the video");
                    }

                    if (mediaTypeResponse.getBody().getStatus().equals("success")) {
                        if (videoConfig.getValidMimeTypes().contains(mediaTypeResponse.getBody().getMediaType())) {
                            video.setStatus(VideoStatusEnum.AVAILABLE);
                            video.setMime(mediaTypeResponse.getBody().getMediaType());
                            video = videoRepository.save(video);
                        }
                        else {
                            logger.log(Level.INFO, "Failed to upload video, invalid type: " + mediaTypeResponse.getBody().getMediaType());
                            s3Utils.deleteFile(video.getPath());
                            video.setStatus(VideoStatusEnum.INVALID);
                            video = videoRepository.save(video);
                            return VideoUploadResponse.fail("Invalid video file type");
                        }
                    }
                    else {
                        logger.log(Level.SEVERE, "getMediaType failed with error message: " + mediaTypeResponse.getBody().getMessage());
                        s3Utils.deleteFile(video.getPath());
                        video.setStatus(VideoStatusEnum.INVALID);
                        video = videoRepository.save(video);
                        return VideoUploadResponse.fail("Failed to process the video");
                    }

                }
                catch (AwsServiceException exception) {
                    //Something happened abort to cleanup
                    s3Utils.abortMultipartUpload(response.uploadId());
                    video.setStatus(VideoStatusEnum.UPLOAD_FAILED);
                    video = videoRepository.save(video);
                    logger.log(Level.SEVERE, "Failed to upload: " + exception.getMessage(), exception);
                    return VideoUploadResponse.fail("Failed to store the video");
                }
                logger.info("Video uploaded: " + cloudFrontUtils.getSignedUrl(video.getPath(), 3600));
            }
        }
        if (!neededParams.isEmpty()) {
            return VideoUploadResponse.fail("There are missing parameters");
        }
        else {
            return VideoUploadResponse.success("success", video.getId());
        }
    }

    //TODO user permissions?
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<UUID> hardDeleteVideo(@PathVariable UUID id) {
        Optional<Video> video = videoRepository.findById(id);
        if (video.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        video.get().setStatus(VideoStatusEnum.DELETED);
        //s3Utils.deleteFile(video.get().getPath());
        return new ResponseEntity<>(id, HttpStatus.OK);
    }

    @GetMapping(value = "/soft-delete/{id}")
    public ResponseEntity<UUID> softDeleteVideo(@PathVariable UUID id) {
        Optional<Video> video = videoRepository.findById(id);
        if (video.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        video.get().setStatus(VideoStatusEnum.SOFT_DELETED);
        return new ResponseEntity<>(id, HttpStatus.OK);
    }

    @GetMapping(value = "/soft-deleted")
    public List<Video> getSoftDeleted() {
        return StreamSupport.stream(videoRepository.findAll().spliterator(), false)
                .filter(video -> video.getStatus() == VideoStatusEnum.SOFT_DELETED)
                .collect(Collectors.toList());
    }

    //Temporary delete later
    @RequestMapping("")
    //@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PreAuthorize("hasAuthority('user:read')")
    public String uploadVideo() throws IOException {
        //Delete the resoource too
        return Streams.asString(getClass().getClassLoader().getResourceAsStream("video.html"));
    }

    //Temporary delete later
    @RequestMapping("/type")
    public LambdaResponse<MediaTypeResponseBody> getData(@RequestParam String key) throws Exception {
        return lambdaUtils.getMediaType(key);
    }
    //Temporary delete later
    @RequestMapping("/videos")
    //@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PreAuthorize("hasAuthority('user:read')")
    public List<Video> getVideos() throws IOException {
        return StreamSupport.stream(videoRepository.findAll().spliterator(), false).collect(Collectors.toList());
    }

    //Temporary delete later
    @RequestMapping("/videoLinks")
    //@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PreAuthorize("hasAuthority('user:read')")
    public Map<UUID, String> getVideoLinks() throws IOException {
        return StreamSupport.stream(videoRepository.findAll().spliterator(), false)
            .collect(Collectors.toMap(Video::getId, v -> {
                try {
                    return cloudFrontUtils.getSignedUrl(v.getPath(), 3600);
                } catch (Exception e) {
                    e.printStackTrace();
                    return "";
                }
            }));
    }
}
