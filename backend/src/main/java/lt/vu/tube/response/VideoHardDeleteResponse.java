package lt.vu.tube.response;

import java.io.Serializable;
import java.util.UUID;

public class VideoHardDeleteResponse implements Serializable {
    private String status;
    private String message;
    private UUID id;

    public VideoHardDeleteResponse() {}

    public VideoHardDeleteResponse(String status, String message, UUID id) {
        this.status = status;
        this.message = message;
        this.id = id;
    }

    public VideoHardDeleteResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public static VideoHardDeleteResponse fail (String message) {
        return new VideoHardDeleteResponse("fail", message);
    }

    public static VideoHardDeleteResponse success (String message, UUID id) {
        return new VideoHardDeleteResponse("success", message, id);
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
}
