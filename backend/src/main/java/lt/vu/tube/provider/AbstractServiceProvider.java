package lt.vu.tube.provider;

import lt.vu.tube.services.AWSS3ContentDeliveryServiceImpl;
import lt.vu.tube.services.StorageService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.logging.Logger;

public abstract class AbstractServiceProvider<T> {

    private final Logger logger = Logger.getLogger(this.getClass().toString());
    //if intellij shows error here ignore it
    @Autowired
    List<T> serviceList;

    T service = null;

    public T getService() {
        return service;
    }

    protected void initProvider(String className) {
        service = findService(serviceList, className);
        logger.info("Selected " + (service == null ? "null" : service.getClass().getName()));
    }

    protected T findService(List<T> serviceList, String className) {
        return serviceList.stream().filter(o->o.getClass().getName().equals(className)).findFirst().orElse(null);
    }
}
