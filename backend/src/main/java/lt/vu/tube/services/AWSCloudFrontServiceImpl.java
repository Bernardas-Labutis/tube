package lt.vu.tube.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lt.vu.tube.config.AWSConfig;
import lt.vu.tube.util.AWSUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudfront.model.GetDistributionRequest;
import javax.annotation.PostConstruct;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;
import java.util.logging.Logger;

@Component
public class AWSCloudFrontServiceImpl implements ContentDeliveryService {

    private static final Logger logger = Logger.getLogger(AWSCloudFrontServiceImpl.class.toString());

    @Autowired
    private AWSConfig awsConfig;

    private String distributionDomainName;
    private CloudFrontClient cloudFrontClient;
    private PrivateKey privateKey;
    private ObjectMapper objectMapper;

    @PostConstruct
    private void init() throws Exception {
        cloudFrontClient = CloudFrontClient.builder()
                .region(Region.AWS_GLOBAL)
                .credentialsProvider(AWSUtils.getCredentialsProvider(awsConfig.getAccessKeyId(), awsConfig.getAccessKeySecret()))
                .build();
        fetchDistributionDomainName();
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        initKeys();
        objectMapper = new ObjectMapper();
    }

    private void fetchDistributionDomainName() {
        distributionDomainName = cloudFrontClient.getDistribution(GetDistributionRequest.builder()
                .id(awsConfig.getDistributionID())
                .build()).distribution().domainName();
    }

    @Override
    public String getSignedUrl(String path, Integer expiration) throws Exception {
        return getSignedUrl(path, null, expiration);
    }

    //AWS java 2.0 neturi signing utility tai darau pats
    @Override
    public String getSignedUrl(String path, Map<String, String> params, Integer expiration) throws Exception {
        Long expirationDate = System.currentTimeMillis() / 1000 + expiration;
        String baseUrl = String.format("https://%s/%s", distributionDomainName, path);

        if(params != null) {
            baseUrl += "?";
            var iterator = params.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                baseUrl += String.format("%s=%s",
                        URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8),
                        URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)
                );
                if(iterator.hasNext()) {
                    baseUrl += "&";
                }
            }
        }
        //Signature is a policy json with no white spaces encrypted with SHA1 RSA and BASE64
        String signature = buildSignatureJson(baseUrl, expirationDate);
        //Replace url unsafe chars
        signature = Base64Utils.encodeToString(signString(signature)).replace("+", "-").replace("=", "_").replace("/", "~");

        logger.info(String.format("Url for '%s' signed by %s", path, this.getClass().toString()));
        return String.format("%s%sExpires=%d&Signature=%s&Key-Pair-Id=%s", baseUrl, params == null ? '?' : '&', expirationDate, signature, awsConfig.getPublicKeyId());
    }

    private byte[] signString(String input) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        Signature signer = Signature.getInstance("SHA1withRSA");
        signer.initSign(privateKey);
        signer.update(input.getBytes());
        return signer.sign();
    }

    private void initKeys() throws Exception {
        byte[] privateKeyBytes = getClass().getClassLoader().getResourceAsStream("private_key.der").readAllBytes();
        PKCS8EncodedKeySpec privateSpec =  new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        privateKey = keyFactory.generatePrivate(privateSpec);
    }

    private String buildSignatureJson(String url, Long expirationDate) throws JsonProcessingException {
        //Žinau kad nėra labai aišku bet tas json 5 lygių
        //Ir šitos 10 eilučių yra geriau negu kurt 4 naujas klases
        //Pirma sudarom statement map nes jam svarbus order
        LinkedHashMap<String, Object> statement = new LinkedHashMap<>();
        statement.put("Resource", url);
        statement.put("Condition",
                Map.of(
                        "DateLessThan", Map.of(
                                "AWS:EpochTime", expirationDate
                        )
                )
        );
        Map<String, Object> jsonMap = Map.of(
                "Statement", List.of(
                        statement
                )
        );
        return objectMapper.writeValueAsString(jsonMap);
    }
}
