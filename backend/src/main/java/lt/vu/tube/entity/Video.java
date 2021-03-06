package lt.vu.tube.entity;

import lt.vu.tube.enums.VideoStatusEnum;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
public class Video {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true)
    private String path;

    private String fileName;

    private Long fileSize;

    @CreationTimestamp
    private Timestamp created;

    @UpdateTimestamp //Change this if we add views or some other non meaningful update
    private Timestamp lastEdited;

    private Boolean isPublic = false;

    private String status = VideoStatusEnum.NONE.name();

    private String mime;

    @ManyToOne
    private AppUser owner;

    @OneToOne(cascade = CascadeType.ALL)
    private VideoShareLink videoShareLink;

    @Version
    private Integer version = 0;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    public Timestamp getLastEdited() {
        return lastEdited;
    }

    public void setLastEdited(Timestamp lastEdited) {
        this.lastEdited = lastEdited;
    }

    public Boolean getPublic() {
        return isPublic;
    }

    public void setPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public VideoStatusEnum getStatus() {
        return VideoStatusEnum.valueOf(status);
    }

    public void setStatus(VideoStatusEnum status) {
        this.status = status.name();
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public AppUser getOwner() {
        return owner;
    }

    public void setOwner(AppUser owner) {
        this.owner = owner;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public VideoShareLink getVideoShareLink() {
        return videoShareLink;
    }

    public void setVideoShareLink(VideoShareLink videoShareLink) {
        this.videoShareLink = videoShareLink;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
