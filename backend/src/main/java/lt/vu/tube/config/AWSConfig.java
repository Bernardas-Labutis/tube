package lt.vu.tube.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:secret.properties")
public class AWSConfig {
    @Value("${tube.aws.distribution.id}")
    private String distributionID;
    @Value("${tube.aws.access.key.id}")
    private String accessKeyId;
    @Value("${tube.aws.access.key.secret}")
    private String accessKeySecretId;
    @Value("${tube.aws.keys.public.id}")
    private String publicKeyId;
    @Value("${tube.aws.region}")
    private String region;
    @Value("${tube.aws.bucket.name}")
    private String bucketName;
    @Value("${tube.aws.lambda.getMediaType}")
    private String getMediaTypeFunctionName;

    public String getDistributionID() {
        return distributionID;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecretId;
    }

    public String getPublicKeyId() {
        return publicKeyId;
    }

    public String getRegion() {
        return region;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getGetMediaTypeFunctionName() {
        return getMediaTypeFunctionName;
    }
}
