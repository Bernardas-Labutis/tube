package lt.vu.tube.repository;

import lt.vu.tube.entity.AppUser;
import lt.vu.tube.entity.Video;
import lt.vu.tube.entity.VideoShareLink;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface VideoShareLinkRepository  extends CrudRepository<VideoShareLink, UUID> {
}
