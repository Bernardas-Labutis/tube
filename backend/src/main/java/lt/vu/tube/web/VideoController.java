package lt.vu.tube.web;

import lt.vu.tube.entity.TestUser;
import lt.vu.tube.util.AWSCloudFrontUtils;
import lt.vu.tube.util.AWSS3Utils;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.util.Streams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.logging.Logger;

@RestController
public class VideoController {

    @Autowired
    AWSS3Utils s3Utils;

    @Autowired
    AWSCloudFrontUtils cloudFrontUtils;

    @RequestMapping("/video/upload")
    public String uploadVideo(HttpServletRequest request) throws Exception {
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart) {
            return "Not multipart!";
        }
        ServletFileUpload upload = new ServletFileUpload();
        FileItemIterator iterStream = upload.getItemIterator(request);
        while (iterStream.hasNext()) {
            FileItemStream item = iterStream.next();
            String name = item.getFieldName();
            InputStream stream = item.openStream();
            Logger.getGlobal().info(name);
            item.getHeaders().getHeaderNames().forEachRemaining(headerName->Logger.getGlobal().info(headerName + ": " + item.getHeaders().getHeader(headerName)));

            if (!item.isFormField()) {
                String filename = ContentDisposition.parse(item.getHeaders().getHeader("content-disposition")).getFilename();
                var response = s3Utils.createMultipartUpload(filename, item.getHeaders().getHeader("content-type"));
                byte[] bytes;
                do {
                    bytes = stream.readNBytes(1024 * 1024 * 10);
                    Logger.getGlobal().info(name + " " + bytes.length);
                    if (bytes.length != 0) {
                        s3Utils.uploadPart(response.uploadId(), bytes);
                    }
                } while(bytes.length != 0);
                Logger.getGlobal().info(name + " " + bytes.length);
                s3Utils.completeMultipartUpload(response.uploadId());
                Logger.getGlobal().info(cloudFrontUtils.getSignedUrl(filename, 3600));
                // Process the InputStream
            } else {
                String formFieldValue = Streams.asString(stream);
                Logger.getGlobal().info(name + " " + formFieldValue);
            }
        }
        return "success!";
    }

    @RequestMapping("/video")
    @Transactional
    public String uploadVideo() throws IOException {
        return Streams.asString(getClass().getClassLoader().getResourceAsStream("video.html"));
    }
}
