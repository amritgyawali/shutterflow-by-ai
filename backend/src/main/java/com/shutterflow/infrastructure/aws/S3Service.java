package com.shutterflow.infrastructure.aws;

import com.shutterflow.core.common.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    /**
     * Uploads raw binary file assets to the target S3 bucket bucket.
     */
    public void uploadFile(String key, byte[] bytes, String contentType) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));
            log.info("Successfully uploaded file asset to S3: {}", key);
        } catch (S3Exception e) {
            log.error("Failed S3 file upload for key: {}. Error: {}", key, e.awsErrorDetails().errorMessage());
            throw new AppException("Failed to upload image file assets to cloud storage", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Generates a secure, cryptographically signed, short-lived read URL.
     */
    public String generatePreSignedUrl(String key, Duration expiration) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(expiration)
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toString();
        } catch (Exception e) {
            log.error("Failed to generate S3 pre-signed link for key: {}. Error: {}", key, e.getMessage());
            throw new AppException("Failed to obtain secure image read link", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes target file asset key from S3 store.
     */
    public void deleteFile(String key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("Successfully deleted S3 file asset: {}", key);
        } catch (S3Exception e) {
            log.error("Failed S3 deletion for key: {}. Error: {}", key, e.awsErrorDetails().errorMessage());
            throw new AppException("Failed to delete image asset from cloud storage", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
