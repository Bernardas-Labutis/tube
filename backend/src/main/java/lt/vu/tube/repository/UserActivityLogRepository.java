package lt.vu.tube.repository;

import lt.vu.tube.entity.UserActivityLog;
import org.springframework.data.repository.CrudRepository;

public interface UserActivityLogRepository extends CrudRepository<UserActivityLog, Long> {
}
