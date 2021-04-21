package lt.vu.tube.dao;

import lt.vu.tube.entity.Video;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface VideoDao extends CrudRepository<Video, UUID> {

}
