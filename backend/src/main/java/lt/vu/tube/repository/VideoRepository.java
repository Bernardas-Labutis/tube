package lt.vu.tube.repository;

import lt.vu.tube.entity.Video;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface VideoRepository extends CrudRepository<Video, UUID> {

}
