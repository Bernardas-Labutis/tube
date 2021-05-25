package lt.vu.tube.provider;

import lt.vu.tube.config.ProviderConfig;
import lt.vu.tube.services.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

@Component
public class StorageServiceProvider extends AbstractServiceProvider<StorageService> {
    @Autowired
    private ProviderConfig providerConfig;

    @PostConstruct
    private void init() {
        initProvider(providerConfig.getStorageServiceClassName());
    }
}
