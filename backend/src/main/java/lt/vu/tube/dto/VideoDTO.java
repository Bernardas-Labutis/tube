package lt.vu.tube.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Getter
@Setter
public class VideoDTO {
    private String id;
    private String key;
    private String title;
    private String uploadTime;
    private String size;
    private Boolean privacy;
    private String ownerUsername;
    private Integer version;

    @JsonIgnore
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss ", Locale.ENGLISH);

    public VideoDTO(String id, String key, String title, Timestamp uploadTime, Long size, boolean isPublic) {
        this(id, key, title, uploadTime, size, isPublic, null, null);
    }

    public VideoDTO(String id, String key, String title, Timestamp uploadTime, Long size, boolean isPublic, String ownerUsername) {
        this(id, key, title, uploadTime, size, isPublic, ownerUsername, null);
    }

    public VideoDTO(String id, String key, String title, Timestamp uploadTime, Long size, boolean isPublic, Integer version) {
        this(id, key, title, uploadTime, size, isPublic, null, version);
    }

    public VideoDTO(String id, String key, String title, Timestamp uploadTime, Long size, boolean isPublic, String ownerUsername, Integer version) {
        this.id = id;
        this.key = key;
        this.title = title;
        this.uploadTime = uploadTime.toLocalDateTime().format(formatter);
        this.size = FileUtils.byteCountToDisplaySize(size);
        this.privacy = isPublic;
        this.ownerUsername = ownerUsername;
        this.version = version;
    }
}
