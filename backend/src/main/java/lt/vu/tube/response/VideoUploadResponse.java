package lt.vu.tube.response;

import java.io.Serializable;
import java.util.UUID;

public class VideoUploadResponse implements Serializable {

    public String status;
    public String message;
    public UUID id;
    public VideoUploadResponse() {}

    public VideoUploadResponse(String status, String message, UUID id) {
        this.status = status;
        this.message = message;
        this.id = id;
    }

    public VideoUploadResponse(String status, String message) {
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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public static VideoUploadResponse fail (String message) {
        return new VideoUploadResponse("fail", message);
    }

    public static VideoUploadResponse success (String message, UUID id) {
        return new VideoUploadResponse("success", message, id);

    }
}
