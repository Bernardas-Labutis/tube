package lt.vu.tube.services;

import software.amazon.awssdk.services.s3.model.*;

public interface StorageService {
    CreateMultipartUploadResponse createMultipartUpload(String path, String contentType);

    CreateMultipartUploadResponse createMultipartUpload(String path);

    UploadPartResponse uploadPart(String uploadId, Integer partNumber, byte[] part);

    UploadPartResponse uploadPart(String uploadId, byte[] part);

    CompleteMultipartUploadResponse completeMultipartUpload(String uploadId);

    AbortMultipartUploadResponse abortMultipartUpload(String uploadId);

    PutObjectResponse uploadFileFromBytes(String path, byte[] bytes, String contentType);

    PutObjectResponse uploadFileFromBytes(String path, byte[] bytes);

    //API neturi move funkcijos reik copy ir delete daryt
    MoveObjectResponse renameFile(String from, String to);

    HeadObjectResponse getFileInfo(String path);

    DeleteObjectResponse deleteFile(String path);

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
}
