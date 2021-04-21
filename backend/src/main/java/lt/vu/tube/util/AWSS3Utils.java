package lt.vu.tube.util;

import lt.vu.tube.config.AWSConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class AWSS3Utils {

    @Autowired
    AWSConfig awsConfig;

    private S3Client s3Client;

    // multipart upload map: uploadId->{partNumber->eTag}
    private Map<String, MultipartUploadPartContainer> multipartUploadMap;

    @PostConstruct
    private void init() {
        s3Client = S3Client.builder()
                .region(AWSUtils.getRegion(awsConfig.getRegion()))
                .credentialsProvider(AWSUtils.getCredentialsProvider(awsConfig.getAccessKeyId(), awsConfig.getAccessKeySecret()))
                .build();
        multipartUploadMap = new HashMap<>();
    }

    public CreateMultipartUploadResponse createMultipartUpload(String path, String contentType) {
        CreateMultipartUploadResponse response = s3Client.createMultipartUpload(CreateMultipartUploadRequest.builder()
                .bucket(awsConfig.getBucketName())
                .key(path)
                .contentType(contentType)
                .build());
        //TreeMap so the entries are ordered
        multipartUploadMap.put(response.uploadId(), new MultipartUploadPartContainer(path));
        return response;
    }

    public CreateMultipartUploadResponse createMultipartUpload(String path) {
        return createMultipartUpload(path, "binary/octet-stream");
    }

    public UploadPartResponse uploadPart(String uploadId, Integer partNumber, byte[] part) {
        UploadPartResponse response = s3Client.uploadPart(UploadPartRequest.builder()
                .bucket(awsConfig.getBucketName())
                .uploadId(uploadId)
                .key(multipartUploadMap.get(uploadId).getPath())
                .partNumber(partNumber)
                .build(), RequestBody.fromBytes(part));
        multipartUploadMap.get(uploadId).getParts().put(partNumber, response.eTag());
        return response;
    }

    public UploadPartResponse uploadPart(String uploadId, byte[] part) {
        Integer partNumber = 1;
        if (!multipartUploadMap.get(uploadId).getParts().isEmpty()) {
            partNumber = multipartUploadMap.get(uploadId).getParts().lastKey() + 1;
        }
        return uploadPart(uploadId, partNumber, part);
    }

    public CompleteMultipartUploadResponse completeMultipartUpload(String uploadId) {
        CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder().parts(
                multipartUploadMap.get(uploadId).getParts().entrySet().stream()
                        .map(entry -> CompletedPart.builder()
                                .partNumber(entry.getKey())
                                .eTag(entry.getValue())
                                .build())
                        .collect(Collectors.toList())
        ).build();
        //jei čia nulūžta dėl upload size too small reiškia parts buvo mažesni už 5 MB, tik paskutinis part gali būt mažesnis už 5 * 1024 * 1024
        CompleteMultipartUploadResponse response = s3Client.completeMultipartUpload(CompleteMultipartUploadRequest.builder()
                .bucket(awsConfig.getBucketName())
                .key(multipartUploadMap.get(uploadId).getPath())
                .uploadId(uploadId)
                .multipartUpload(completedMultipartUpload)
                .build());
        multipartUploadMap.remove(uploadId);
        return response;
    }

    public AbortMultipartUploadResponse abortMultipartUpload(String uploadId) {
        AbortMultipartUploadResponse response = s3Client.abortMultipartUpload(AbortMultipartUploadRequest.builder()
                .bucket(awsConfig.getBucketName())
                .key(multipartUploadMap.get(uploadId).getPath())
                .uploadId(uploadId)
                .build());
        multipartUploadMap.remove(uploadId);
        return response;
    }

    public PutObjectResponse uploadFileFromBytes(String path, byte[] bytes, String contentType) {
        return s3Client.putObject(PutObjectRequest.builder()
                .bucket(awsConfig.getBucketName())
                .key(path)
                .contentType(contentType)
                .build(), RequestBody.fromBytes(bytes));
    }

    public PutObjectResponse uploadFileFromBytes(String path, byte[] bytes) {
        return uploadFileFromBytes(path, bytes, "binary/octet-stream");
    }

    //API neturi move funkcijos reik copy ir delete daryt
    public MoveObjectResponse renameFile(String from, String to) {
        CopyObjectResponse copyObjectResponse = s3Client.copyObject(CopyObjectRequest.builder()
                .copySource(String.format("%s/%s", awsConfig.getBucketName(), from))
                .destinationBucket(awsConfig.getBucketName())
                .destinationKey(to)
                .build());
        //Jeigu failed meta exception, tikiuosi taip visda, nes jei ne prarast failus galim
        DeleteObjectResponse deleteObjectResponse = s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(awsConfig.getBucketName())
                .key(from)
                .build());
        return new MoveObjectResponse(copyObjectResponse, deleteObjectResponse);
    }

    public HeadObjectResponse getFileInfo(String path) {
        HeadObjectResponse headObjectResponse = s3Client.headObject(HeadObjectRequest.builder()
                .bucket(awsConfig.getBucketName())
                .key(path)
                .build());
        return headObjectResponse;
    }

    public DeleteObjectResponse deleteFile(String path) {
        DeleteObjectResponse deleteObjectResponse = s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(awsConfig.getBucketName())
                .key(path)
                .build());
        return deleteObjectResponse;
    }

    public static class MoveObjectResponse {
        private CopyObjectResponse copyObjectResponse;
        private DeleteObjectResponse deleteObjectResponse;

        public MoveObjectResponse() {
        }

        public MoveObjectResponse(CopyObjectResponse copyObjectResponse, DeleteObjectResponse deleteObjectResponse) {
            this.copyObjectResponse = copyObjectResponse;
            this.deleteObjectResponse = deleteObjectResponse;
        }

        public CopyObjectResponse getCopyObjectResponse() {
            return copyObjectResponse;
        }

        public void setCopyObjectResponse(CopyObjectResponse copyObjectResponse) {
            this.copyObjectResponse = copyObjectResponse;
        }

        public DeleteObjectResponse getDeleteObjectResponse() {
            return deleteObjectResponse;
        }

        public void setDeleteObjectResponse(DeleteObjectResponse deleteObjectResponse) {
            this.deleteObjectResponse = deleteObjectResponse;
        }
    }

    private static class MultipartUploadPartContainer {
        private SortedMap<Integer, String> parts;
        private String path;

        public MultipartUploadPartContainer() {
            parts = new TreeMap<>();
        }

        public MultipartUploadPartContainer(String path) {
            this();
            this.path = path;
        }

        public SortedMap<Integer, String> getParts() {
            return parts;
        }

        public void setParts(SortedMap<Integer, String> parts) {
            this.parts = parts;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

    }
}
