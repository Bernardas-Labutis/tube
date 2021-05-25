package lt.vu.tube.services;

import lt.vu.tube.config.AWSConfig;
import lt.vu.tube.util.AWSUtils;
import lt.vu.tube.web.VideoController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Map;
import java.util.logging.Logger;

//Extensibility pavyzdys
//Duoda failus tiesiai iš storage o ner per CDN
@Component
public class AWSS3ContentDeliveryServiceImpl implements ContentDeliveryService {

    private static final Logger logger = Logger.getLogger(AWSS3ContentDeliveryServiceImpl.class.toString());

    @Autowired
    private AWSConfig awsConfig;

    private S3Presigner presigner;

    @PostConstruct
    private void init() {
        presigner = S3Presigner.builder()
                .region(AWSUtils.getRegion(awsConfig.getRegion()))
                .credentialsProvider(AWSUtils.getCredentialsProvider(awsConfig.getAccessKeyId(), awsConfig.getAccessKeySecret()))
                .build();
    }
    @Override
    public String getSignedUrl(String path, Integer expiration) throws Exception {
        return getSignedUrl(path, null ,expiration);
    }

    @Override
    public String getSignedUrl(String path, Map<String, String> params, Integer expiration) throws Exception {
        var getObjectResponseBuilder = GetObjectRequest.builder()
                .bucket(awsConfig.getBucketName())
                .key(path);
        //Only this param supported here
        if (params != null && params.containsKey("response-content-disposition")) {
            getObjectResponseBuilder.responseContentDisposition(params.get("response-content-disposition"));
        }

        var result = presigner.presignGetObject(GetObjectPresignRequest.builder()
                .getObjectRequest(getObjectResponseBuilder.build())
                .signatureDuration(Duration.ofSeconds(expiration))
                .build());

        logger.info(String.format("Url for '%s' signed by %s", path, this.getClass().toString()));
        return result.url().toString();
    }
}
