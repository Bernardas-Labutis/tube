package lt.vu.tube.repository;

import lt.vu.tube.entity.AppUser;
import lt.vu.tube.entity.Video;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VideoRepository extends CrudRepository<Video, UUID> {
    Iterable<Video> findVideosByOwner(AppUser owner);
    List<Video>  findVideosByStatusAndIsPublic(String status, boolean isPublic);
}
