package lt.vu.tube.dto;

import lombok.Getter;
import lombok.Setter;

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

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss ", Locale.ENGLISH);

    public VideoDTO(String id, String key, String title, Timestamp uploadTime, Long size) {
        this.id = id;
        this.key = key;
        this.title = title;
        this.uploadTime = uploadTime.toLocalDateTime().format(formatter);
        this.size = size / 1000000 + " MB";
    }
}
