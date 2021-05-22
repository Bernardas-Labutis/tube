package lt.vu.tube.services;

import java.util.Map;

public interface ContentDeliveryService {
    String getSignedUrl(String path, Integer expiration) throws Exception;
    String getSignedUrl(String path, Map<String, String> params, Integer expiration) throws Exception;
}
