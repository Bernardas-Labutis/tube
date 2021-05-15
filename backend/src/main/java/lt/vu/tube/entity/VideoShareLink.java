package lt.vu.tube.entity;

import javax.persistence.*;
import java.util.UUID;

@Entity
public class VideoShareLink {
    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(mappedBy = "videoShareLink")
    private Video video;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Video getVideo() {
        return video;
    }

    public void setVideo(Video video) {
        this.video = video;
    }

    @PreRemove
    public void preDelete() {
        if (video != null) {
            video.setVideoShareLink(null);
        }
    }
}
