package lt.vu.tube.model;

public class MediaTypeResponseBody {
    private String status;
    private String message;
    private String mediaType;

    public MediaTypeResponseBody() {}

    public MediaTypeResponseBody(String status, String message, String mediaType) {
        this.status = status;
        this.message = message;
        this.mediaType = mediaType;
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

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }
}