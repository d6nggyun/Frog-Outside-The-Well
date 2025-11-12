package com._oormthon.seasonthon.domain.s3.service;

import com._oormthon.seasonthon.domain.member.dto.res.UserResponse;
import com._oormthon.seasonthon.domain.member.service.UserService;
import com._oormthon.seasonthon.domain.s3.dto.PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

        private final S3Presigner s3Presigner;
        private final UserService userService;

        @Value("${cloud.s3.bucket}")
        private String bucket;

        @Value("${cloud.aws.region.static}")
        private String region;

        /**
         * Presigned URL 발급 (파일 타입 제공)
         */
        public PresignedUrlResponse generateUploadPresignedUrl(Long userId, String fileName, String fileType) {
                return createPresignedUrl(userId, fileName, fileType);
        }

        /**
         * Presigned URL 발급 (파일 타입 미제공 시 자동 추론)
         */
        public PresignedUrlResponse generateUploadPresignedUrlWithoutFileType(Long userId, String fileName) {
                String fileType = determineContentTypeFromKey(fileName);
                return createPresignedUrl(userId, fileName, fileType);
        }

        /**
         * S3 객체 삭제
         */
        public void deleteFile(Long userId, String fileName) {
                String key = buildS3Key(userService.getUserById(userId).nickname(), fileName);

                try (S3Client s3 = S3Client.builder()
                                .region(Region.of(region))
                                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                                .build()) {

                        s3.deleteObject(DeleteObjectRequest.builder()
                                        .bucket(bucket)
                                        .key(key)
                                        .build());
                }
        }

        /**
         * Presigned URL 생성 로직 (공통)
         */
        private PresignedUrlResponse createPresignedUrl(Long userId, String fileName, String fileType) {
                UserResponse user = userService.getUserById(userId);
                String key = buildS3Key(user.nickname(), fileName);

                PutObjectRequest putRequest = PutObjectRequest.builder()
                                .bucket(bucket)
                                .key(key)
                                .contentType(fileType)
                                .build();

                PresignedPutObjectRequest presignedRequest = s3Presigner
                                .presignPutObject(builder -> builder.putObjectRequest(putRequest)
                                                .signatureDuration(Duration.ofMinutes(10)));

                return new PresignedUrlResponse(presignedRequest.url().toString(), key);
        }

        /**
         * S3 키 생성: 닉네임/UUID-파일명
         */
        private String buildS3Key(String nickname, String fileName) {
                return String.format("%s/%s-%s", nickname, UUID.randomUUID(), fileName);
        }

        /**
         * 파일 확장자 기반 Content-Type 추론
         */
        private String determineContentTypeFromKey(String key) {
                if (key == null)
                        return "application/octet-stream";

                String lowerKey = key.toLowerCase();
                if (lowerKey.endsWith(".jpg") || lowerKey.endsWith(".jpeg"))
                        return "image/jpeg";
                if (lowerKey.endsWith(".png"))
                        return "image/png";
                if (lowerKey.endsWith(".gif"))
                        return "image/gif";
                if (lowerKey.endsWith(".webp"))
                        return "image/webp";
                if (lowerKey.endsWith(".svg"))
                        return "image/svg+xml";

                return "application/octet-stream";
        }
}
