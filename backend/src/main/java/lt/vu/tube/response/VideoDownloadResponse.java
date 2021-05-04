package lt.vu.tube.response;

import java.util.UUID;

public class VideoDownloadResponse {

    public String status;
    public String message;
    public String url;

    public VideoDownloadResponse() {}

    public VideoDownloadResponse(String status, String message, String url) {
        this.status = status;
        this.message = message;
        this.url = url;
    }

    public VideoDownloadResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public static VideoDownloadResponse fail (String message) {
        return new VideoDownloadResponse("fail", message);
    }

    public static VideoDownloadResponse success (String message, String url) {
        return new VideoDownloadResponse("success", message, url);
    }
}
