package lt.vu.tube.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProviderConfig {
    @Value("${tube.provider.contentdeliveryservice.classname:lt.vu.tube.services.AWSCloudFrontContentDeliveryServiceImpl}")
    private String contentDeliveryServiceClassName;
    @Value("${tube.provider.functionservice.classname:lt.vu.tube.services.AWSLambdaFunctionServiceImpl}")
    private String functionServiceClassName;
    @Value("${tube.provider.storageservice.classname:lt.vu.tube.services.AWSS3StorageServiceImpl}")
    private String storageServiceClassName;

    public String getContentDeliveryServiceClassName() {
        return contentDeliveryServiceClassName;
    }

    public String getFunctionServiceClassName() {
        return functionServiceClassName;
    }

    public String getStorageServiceClassName() {
        return storageServiceClassName;
    }
}
