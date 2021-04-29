package lt.vu.tube.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lt.vu.tube.config.AWSConfig;
import lt.vu.tube.model.LambdaResponse;
import lt.vu.tube.model.MediaTypeResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import javax.annotation.PostConstruct;
import java.util.logging.Logger;

@Component
public class AWSLambdaUtils {

    @Autowired
    private AWSConfig awsConfig;

    private LambdaClient lambdaClient;
    private ObjectMapper objectMapper;

    @PostConstruct
    private void init() throws Exception {
        lambdaClient = LambdaClient.builder()
                .region(AWSUtils.getRegion(awsConfig.getRegion()))
                .credentialsProvider(AWSUtils.getCredentialsProvider(awsConfig.getAccessKeyId(), awsConfig.getAccessKeySecret()))
                .build();
        objectMapper = new ObjectMapper();
    }

    //Key yra path to file
    public LambdaResponse<MediaTypeResponseBody> getMediaType(String key) throws JsonProcessingException {
        String payloadJSON = objectMapper.writeValueAsString(new MediaTypeRequest(key));
        InvokeRequest request = InvokeRequest.builder()
                .functionName(awsConfig.getGetMediaTypeFunctionName())
                //object -> jsonString -> sdkBytes
                .payload(SdkBytes.fromUtf8String(payloadJSON))
                .build();
        InvokeResponse response = lambdaClient.invoke(request);
        return objectMapper.readValue(response.payload().asUtf8String(), new TypeReference<LambdaResponse<MediaTypeResponseBody>>() {});
    }

    private static class MediaTypeRequest {
        private String key;

        public MediaTypeRequest() {}

        public MediaTypeRequest(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }
}
