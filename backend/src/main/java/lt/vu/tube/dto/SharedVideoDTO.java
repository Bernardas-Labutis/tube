package lt.vu.tube.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
@Getter
@Setter
public class SharedVideoDTO extends VideoDTO {
    private String viewUrl;
    private String downloadUrl;

    public SharedVideoDTO(String id, String key, String title, Timestamp uploadTime, Long size, boolean isPrivate, String ownerName, String viewUrl, String downloadUrl) {
        super(id, key, title, uploadTime, size, isPrivate, ownerName);
        this.viewUrl = viewUrl;
        this.downloadUrl = downloadUrl;
    }
}
