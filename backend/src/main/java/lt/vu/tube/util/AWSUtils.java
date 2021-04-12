package lt.vu.tube.util;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;

public class AWSUtils {
    public static Region getRegion(String region) {
        return Region.regions().stream().filter(r -> r.id().equals(region)).findAny().orElse(null);
    }

    public static AwsCredentialsProvider getCredentialsProvider(String accessKeyId, String accessKeySecret) {
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, accessKeySecret));
    }
}
