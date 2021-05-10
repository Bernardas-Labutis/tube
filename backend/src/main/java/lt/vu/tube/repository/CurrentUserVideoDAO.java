package lt.vu.tube.repository;

import lt.vu.tube.entity.AppUser;
import lt.vu.tube.entity.Video;
import lt.vu.tube.enums.VideoStatusEnum;
import lt.vu.tube.services.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collections;
import java.util.List;

@Component
public class CurrentUserVideoDAO {
    @PersistenceContext
    private EntityManager em;

    private final AuthenticatedUser authenticatedUser;

    @Autowired
    public CurrentUserVideoDAO (AuthenticatedUser authenticatedUser){
        this.authenticatedUser = authenticatedUser;
    }

    public List<Video> getCurrentUserAvailableVideos(){
        AppUser currentUser = authenticatedUser.getAuthenticatedUser();
        if(currentUser == null) {
            return Collections.emptyList();
        }

        return em.createQuery("select v from Video v where v.owner = :currentUser and v.status = :videoStatus", Video.class)
                .setParameter("currentUser", currentUser)
                .setParameter("videoStatus", VideoStatusEnum.AVAILABLE.name())
                .getResultList();
    }

    public List<Video> getCurrentUserSoftDeletedVideos(){
        AppUser currentUser = authenticatedUser.getAuthenticatedUser();
        if(currentUser == null) {
            return Collections.emptyList();
        }

        return em.createQuery("select v from Video v where v.owner = :currentUser and v.status = :videoStatus", Video.class)
                .setParameter("currentUser", currentUser)
                .setParameter("videoStatus", VideoStatusEnum.SOFT_DELETED.name())
                .getResultList();
    }
}
