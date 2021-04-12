package lt.vu.tube.util;

import lt.vu.tube.config.AWSConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudfront.model.GetDistributionRequest;
import javax.annotation.PostConstruct;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
@Component
public class AWSCloudFrontUtils {

    @Autowired
    AWSConfig awsConfig;

    private String distributionDomainName;
    private CloudFrontClient cloudFrontClient;
    private PrivateKey privateKey;

    @PostConstruct
    private void init() throws Exception {
        cloudFrontClient = CloudFrontClient.builder()
                .region(Region.AWS_GLOBAL)
                .credentialsProvider(AWSUtils.getCredentialsProvider(awsConfig.getAccessKeyId(), awsConfig.getAccessKeySecret()))
                .build();
        fetchDistributionDomainName();
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        initKeys();
    }

    private void fetchDistributionDomainName() {
        distributionDomainName = cloudFrontClient.getDistribution(GetDistributionRequest.builder()
                .id(awsConfig.getDistributionID())
                .build()).distribution().domainName();
    }

    //AWS java 2.0 neturi signing utility tai darau pats
    public String getSignedUrl(String path, Integer expiration) throws Exception {
        Long expirationDate = System.currentTimeMillis() / 1000 + expiration;
        String baseUrl = String.format("https://%s/%s", distributionDomainName, path);
        //Signature is a policy json with no white spaces encrypted with SHA1 RSA and BASE64
        String signature = String.format("{\"Statement\":[{\"Resource\":\"%s\",\"Condition\":{\"DateLessThan\":{\"AWS:EpochTime\":%d}}}]}", baseUrl, expirationDate);
        //Replace url unsafe chars
        signature = Base64Utils.encodeToString(signString(signature)).replace("+", "-").replace("=", "_").replace("/", "~");
        return String.format("%s?Expires=%d&Signature=%s&Key-Pair-Id=%s", baseUrl, expirationDate, signature, awsConfig.getPublicKeyId());
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
}
