package lt.vu.tube.web;

import lt.vu.tube.config.VideoConfig;
import lt.vu.tube.dto.SharedVideoDTO;
import lt.vu.tube.dto.VideoDTO;
import lt.vu.tube.entity.AppUser;
import lt.vu.tube.entity.Video;
import lt.vu.tube.entity.VideoShareLink;
import lt.vu.tube.enums.VideoStatusEnum;
import lt.vu.tube.model.LambdaResponse;
import lt.vu.tube.model.MediaTypeResponseBody;
import lt.vu.tube.provider.ContentDeliveryServiceProvider;
import lt.vu.tube.provider.FunctionServiceProvider;
import lt.vu.tube.provider.StorageServiceProvider;
import lt.vu.tube.repository.AppUserRepository;
import lt.vu.tube.repository.CurrentUserVideoDAO;
import lt.vu.tube.repository.VideoRepository;
import lt.vu.tube.repository.VideoShareLinkRepository;
import lt.vu.tube.requests.RenameRequest;
import lt.vu.tube.response.VideoDownloadResponse;
import lt.vu.tube.response.VideoStorageResponse;
import lt.vu.tube.response.VideoUploadResponse;
import lt.vu.tube.services.*;
import lt.vu.tube.web.mapper.VideoMapper;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.util.Streams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping(value = "/video")
public class VideoController {

    private static final Logger logger = Logger.getLogger(VideoController.class.toString());
    @Autowired
    VideoConfig videoConfig;
    @Autowired
    AuthenticatedUser authenticatedUser;
    @Autowired
    CurrentUserVideoDAO currentUserVideoDAO;
    @Autowired
    VideoShareLinkRepository videoShareLinkRepository;
    @Autowired
    private VideoRepository videoRepository;
    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private ContentDeliveryServiceProvider contentDeliveryServiceProvider;
    private ContentDeliveryService contentDeliveryService;

    @Autowired
    private StorageServiceProvider storageServiceProvider;
    private StorageService storageService;

    @Autowired
    private FunctionServiceProvider functionServiceProvider;
    private FunctionService functionService;

    @PostConstruct
    private void init() {
        contentDeliveryService = contentDeliveryServiceProvider.getService();
        storageService = storageServiceProvider.getService();
        functionService = functionServiceProvider.getService();
    }

    @RequestMapping(value = "/upload")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    //@PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<VideoUploadResponse> uploadVideo(HttpServletRequest request) throws Exception {
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart) {
            return new ResponseEntity<>(VideoUploadResponse.fail("Request must be multipart"), HttpStatus.BAD_REQUEST);
        }
        Set<String> neededParams = new HashSet<>(Set.of("fileName", "fileSize", "file"));


        String fileName = "";
        Long reportedFileSize = 0L;

        Video video = null;

        ServletFileUpload upload = new ServletFileUpload();
        FileItemIterator iterStream = upload.getItemIterator(request);
        AppUser user = authenticatedUser.getAuthenticatedUser();
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
                            reportedFileSize = Long.parseLong(value);
                            break;
                    }
                }
            } else if (neededParams.contains(item.getFieldName())) {
                if (neededParams.size() == 1) {
                    neededParams.remove(item.getFieldName());
                } else {
                    return new ResponseEntity<>(VideoUploadResponse.fail("There are missing parameters or they are in wrong order"), HttpStatus.BAD_REQUEST);
                }
                if (user.getMaxStorage() < user.getUsedStorage() + reportedFileSize) {
                    return new ResponseEntity<>(VideoUploadResponse.fail("Not enough space in storage"), HttpStatus.FORBIDDEN);
                }

                //No transaction because the operation is long and we might need to check the status midway from a different call
                //Create video object to get the path we're going to use
                video = new Video();
                video.setFileName(fileName);
                video.setOwner(user);
                video = videoRepository.save(video);

                //Start upload
                video.setPath("videos/" + video.getId().toString());
                video.setMime(item.getHeaders().getHeader("content-type")); //Later update this after upload by extracting metadata
                var response = storageService.createMultipartUpload(video.getPath(), item.getHeaders().getHeader("content-type"));
                video.setStatus(VideoStatusEnum.UPLOADING);
                video = videoRepository.save(video);

                try {
                    long fileLength = 0L;
                    byte[] bytes;
                    do {
                        bytes = stream.readNBytes(1024 * 1024 * 10);
                        if (bytes.length != 0) {
                            fileLength += bytes.length;
                            if (user.getMaxStorage() < user.getUsedStorage() + fileLength) {
                                storageService.abortMultipartUpload(response.uploadId());
                                video.setStatus(VideoStatusEnum.UPLOAD_FAILED);
                                video = videoRepository.save(video);
                                logger.log(Level.INFO, String.format("Failed to upload video, reported filesize didn't match the real one and user didn't have enough space. (reported: %d)", reportedFileSize));
                                return new ResponseEntity<>(VideoUploadResponse.fail("Not enough space in storage"), HttpStatus.FORBIDDEN);

                            }
                            storageService.uploadPart(response.uploadId(), bytes);
                        }
                    } while (bytes.length != 0);
                    //Finish upload
                    storageService.completeMultipartUpload(response.uploadId());
                    video.setFileSize(fileLength);
                    video.setStatus(VideoStatusEnum.PROCESSING);
                    video = videoRepository.save(video);
                    LambdaResponse<MediaTypeResponseBody> mediaTypeResponse;
                    try {
                        mediaTypeResponse = functionService.getMediaType(video.getPath());
                    } catch (Exception exception) {
                        logger.log(Level.SEVERE, "getMediaType failed with exception: " + exception.getMessage(), exception);
                        storageService.deleteFile(video.getPath());
                        video.setStatus(VideoStatusEnum.INVALID);
                        video = videoRepository.save(video);
                        return new ResponseEntity<>(VideoUploadResponse.fail("Failed to process the video"), HttpStatus.INTERNAL_SERVER_ERROR);
                    }

                    if (mediaTypeResponse.getBody().getStatus().equals("success")) {
                        if (videoConfig.getValidMimeTypes().contains(mediaTypeResponse.getBody().getMediaType())) {
                            video.setStatus(VideoStatusEnum.AVAILABLE);
                            video.setMime(mediaTypeResponse.getBody().getMediaType());
                            video.setPublic(false);
                            video.setCreated(new Timestamp(System.currentTimeMillis()));
                            video = videoRepository.save(video);
                        } else {
                            logger.log(Level.INFO, "Failed to upload video, invalid type: " + mediaTypeResponse.getBody().getMediaType());
                            storageService.deleteFile(video.getPath());
                            video.setStatus(VideoStatusEnum.INVALID);
                            video = videoRepository.save(video);
                            return new ResponseEntity<>(VideoUploadResponse.fail("Invalid video file type"), HttpStatus.BAD_REQUEST);
                        }
                    } else {
                        logger.log(Level.SEVERE, "getMediaType failed with error message: " + mediaTypeResponse.getBody().getMessage());
                        storageService.deleteFile(video.getPath());
                        video.setStatus(VideoStatusEnum.INVALID);
                        video = videoRepository.save(video);
                        return new ResponseEntity<>(VideoUploadResponse.fail("Failed to process the video"), HttpStatus.INTERNAL_SERVER_ERROR);
                    }

                } catch (AwsServiceException exception) {
                    //Something happened abort to cleanup
                    storageService.abortMultipartUpload(response.uploadId());
                    video.setStatus(VideoStatusEnum.UPLOAD_FAILED);
                    video = videoRepository.save(video);
                    logger.log(Level.SEVERE, "Failed to upload: " + exception.getMessage(), exception);
                    return new ResponseEntity<>(VideoUploadResponse.fail("Failed to store the video"), HttpStatus.INTERNAL_SERVER_ERROR);
                }
                logger.info("Video uploaded: " + contentDeliveryService.getSignedUrl(video.getPath(), 3600));
            }
        }
        if (!neededParams.isEmpty()) {
            return new ResponseEntity<>(VideoUploadResponse.fail("There are missing parameters"), HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<>(VideoUploadResponse.success("success", video.getId()), HttpStatus.OK);
        }
    }

    @DeleteMapping(value = "/{id}")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ResponseEntity<UUID> hardDeleteVideo(@PathVariable UUID id) {
        Optional<Video> video = videoRepository.findById(id);
        if (video.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        video.get().setStatus(VideoStatusEnum.DELETED);
        videoRepository.save(video.get());
        //TODO uncomment after testings are done
        //s3Utils.deleteFile(video.get().getPath());
        return new ResponseEntity<>(id, HttpStatus.OK);
    }

    @GetMapping(value = "/soft-delete/{id}")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ResponseEntity<UUID> softDeleteVideo(@PathVariable UUID id) {
        Optional<Video> video = videoRepository.findById(id);
        if (video.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        video.get().setStatus(VideoStatusEnum.SOFT_DELETED);
        videoRepository.save(video.get());
        return new ResponseEntity<>(id, HttpStatus.OK);
    }

    @GetMapping(value = "/soft-deleted")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public List<VideoDTO> getSoftDeleted() {
        return StreamSupport.stream(currentUserVideoDAO.getCurrentUserSoftDeletedVideos().spliterator(), false)
                .filter(video -> video.getStatus() == VideoStatusEnum.SOFT_DELETED)
                .map(VideoMapper::mapToVideoDto)
                .collect(Collectors.toList());
    }

    @GetMapping(value = "/recover/{id}")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ResponseEntity<UUID> recoverVideo(@PathVariable UUID id) {
        Optional<Video> video = videoRepository.findById(id);
        if (video.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        video.get().setStatus(VideoStatusEnum.AVAILABLE);
        videoRepository.save(video.get());
        return new ResponseEntity<>(id, HttpStatus.OK);
    }

    @GetMapping(value = "changeVisibility/{id}")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ResponseEntity<UUID> changeVisibility(@PathVariable UUID id, @RequestParam boolean isPublic) {
        Optional<Video> video = videoRepository.findById(id);
        if (video.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        video.get().setPublic(isPublic);
        videoRepository.save(video.get());
        return new ResponseEntity<>(id, HttpStatus.OK);
    }

    @GetMapping(value = "/download/{id}")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ResponseEntity<VideoDownloadResponse> downloadVideo(@PathVariable UUID id) {
        Optional<Video> optionalVideo = videoRepository.findById(id);
        if (optionalVideo.isEmpty()) {
            return new ResponseEntity<>(VideoDownloadResponse.fail("Video with the specified id was not found"), HttpStatus.NOT_FOUND);
        }
        Video video = optionalVideo.get();
        //Only allow to download your own videos
        //Could be change to allow public videos to be downloaded by anyone
        if (video.getOwner() == null || video.getOwner().equals(authenticatedUser.getAuthenticatedUser())) {
            if (video.getStatus() != VideoStatusEnum.AVAILABLE && video.getStatus() != VideoStatusEnum.SOFT_DELETED) {
                return new ResponseEntity<>(VideoDownloadResponse.fail("Video is not available for download"), HttpStatus.BAD_REQUEST);
            }

            MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
            String extension = "";
            try {
                MimeType mimeType = allTypes.forName(video.getMime());
                extension = mimeType.getExtension();
            } catch (MimeTypeException e) {
                logger.log(Level.WARNING, "could not find a mimeType for name: " + video.getMime());
            }

            var contentDisposition = ContentDisposition
                    .builder("attachment")
                    .filename(video.getFileName() + extension)
                    .build();
            try {
                String url = contentDeliveryService.getSignedUrl(video.getPath(), Map.of("response-content-disposition", contentDisposition.toString()), 3600);
                return new ResponseEntity<>(VideoDownloadResponse.success("Success", url), HttpStatus.OK);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error while trying to sign a download url for " + id, e);
                return new ResponseEntity<>(VideoDownloadResponse.fail("Error while trying to sign a download url"), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>(VideoDownloadResponse.fail("Unauthorized"), HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/userAvailable")
    @PreAuthorize("hasAuthority('user:read')")
    public List<VideoDTO> getAllCurrentUserAvailableVideos() throws IOException {
        return StreamSupport.stream(currentUserVideoDAO.getCurrentUserAvailableVideos().spliterator(), false)
                .map(VideoMapper::mapToVideoDto)
                .collect(Collectors.toList());
    }

    @RequestMapping("/viewingUrl/{id}")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<String> getVideoViewingUrl(@PathVariable UUID id) {
        Optional<Video> optionalVideo = videoRepository.findById(id);
        if (optionalVideo.isEmpty()) {
            return new ResponseEntity<>("Video with the specified id was not found", HttpStatus.NOT_FOUND);
        }
        Video video = optionalVideo.get();
        //Allow to view unowned, your own and public videos
        if (video.getOwner() == null || video.getOwner().equals(authenticatedUser.getAuthenticatedUser()) || video.getPublic()) {
            if (video.getStatus() != VideoStatusEnum.AVAILABLE && video.getStatus() != VideoStatusEnum.SOFT_DELETED) {
                return new ResponseEntity<>("Video url not available", HttpStatus.BAD_REQUEST);
            }

            try {
                String url = contentDeliveryService.getSignedUrl(video.getPath(), 3600);
                return new ResponseEntity<>(url, HttpStatus.OK);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error while trying to sign a viewing url for " + id, e);
                return new ResponseEntity<>("Error while trying to sign a viewing url", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }
    }

    @RequestMapping("/userStorage")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<VideoStorageResponse> getCurrentUserUsedBytes() {
        AppUser currentUser = authenticatedUser.getAuthenticatedUser();
        if (currentUser == null) {
            return new ResponseEntity<>(new VideoStorageResponse(), HttpStatus.UNAUTHORIZED);
        } else {
            return new ResponseEntity<>(new VideoStorageResponse(currentUser.getUsedStorage(), currentUser.getMaxStorage()), HttpStatus.OK);
        }
    }

    //Passima i?? video id
    @GetMapping("/share/{videoId}")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ResponseEntity<UUID> getShareId(@PathVariable UUID videoId) {
        AppUser currentUser = authenticatedUser.getAuthenticatedUser();
        Video video = videoRepository.findById(videoId).orElse(null);
        if (video == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (video.getOwner() == null || !video.getOwner().equals(currentUser)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } else {
            if (video.getVideoShareLink() == null) {
                video.setVideoShareLink(new VideoShareLink());
                video = videoRepository.save(video);
            }
            return new ResponseEntity<>(video.getVideoShareLink().getId(), HttpStatus.OK);
        }
    }

    //Trina per shareId
    @DeleteMapping("/share/{shareId}")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ResponseEntity<HttpStatus> deleteShareId(@PathVariable UUID shareId) {
        AppUser currentUser = authenticatedUser.getAuthenticatedUser();
        VideoShareLink link = videoShareLinkRepository.findById(shareId).orElse(null);
        if (link == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (link.getVideo().getOwner() == null || !link.getVideo().getOwner().equals(currentUser)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } else {
            videoShareLinkRepository.delete(link);
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @GetMapping("/share/get/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SharedVideoDTO> getSharedVideo(@PathVariable UUID id) {
        VideoShareLink link = videoShareLinkRepository.findById(id).orElse(null);
        if (link == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (link.getVideo().getStatus() != VideoStatusEnum.AVAILABLE) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } else {
            Video video = link.getVideo();
            String viewUrl;
            String downloadUrl;
            MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
            String extension = "";
            try {
                MimeType mimeType = allTypes.forName(video.getMime());
                extension = mimeType.getExtension();
            } catch (MimeTypeException e) {
                logger.log(Level.WARNING, "Could not find a mimeType for name: " + video.getMime());
            }
            var contentDisposition = ContentDisposition
                    .builder("attachment")
                    .filename(video.getFileName() + extension)
                    .build();
            try {
                viewUrl = contentDeliveryService.getSignedUrl(video.getPath(), 3600);
                downloadUrl = contentDeliveryService.getSignedUrl(video.getPath(), Map.of("response-content-disposition", contentDisposition.toString()), 3600);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error while trying to sign a urls for " + video.getId(), e);
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity<>(new SharedVideoDTO(
                    video.getId().toString(),
                    video.getId().toString(),
                    video.getFileName(),
                    video.getCreated(),
                    video.getFileSize(),
                    video.getPublic(),
                    video.getOwner().getUsername(),
                    viewUrl,
                    downloadUrl
            ), HttpStatus.OK);
        }
    }

    @PostMapping("/rename")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ResponseEntity<UUID> rename(@RequestBody RenameRequest request) {
        AppUser currentUser = authenticatedUser.getAuthenticatedUser();
        Video video = videoRepository.findById(request.getId()).orElse(null);
        if (video == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (video.getOwner() == null || !video.getOwner().equals(currentUser)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } else if (!video.getVersion().equals(request.getVersion())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } else {
            video.setFileName(request.getNewName());
            videoRepository.save(video);
            return new ResponseEntity<>(request.getId(), HttpStatus.OK);
        }
    }

    @GetMapping("/publicAvailable")
    @PreAuthorize("hasAuthority('user:read')")
    public List<VideoDTO> getAllPublicAvailableVideos() throws IOException {
        return StreamSupport.stream(videoRepository.findVideosByStatusAndIsPublic(VideoStatusEnum.AVAILABLE.name(), true).spliterator(), false)
                .map(VideoMapper::mapToVideoDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/userAvailable/{id}")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<VideoDTO> getCurrentUserAvailableVideoById(@PathVariable UUID id) {
        Video video = videoRepository.findVideoByOwnerAndStatusAndId(authenticatedUser.getAuthenticatedUser(), VideoStatusEnum.AVAILABLE.name(), id);
        if (video == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(VideoMapper.mapToVideoDto(video), HttpStatus.OK);
    }
}
