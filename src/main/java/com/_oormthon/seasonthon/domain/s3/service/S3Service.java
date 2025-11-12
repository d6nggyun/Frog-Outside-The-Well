package com._oormthon.seasonthon.domain.s3.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com._oormthon.seasonthon.domain.member.dto.res.UserResponse;
import com._oormthon.seasonthon.domain.member.service.UserService;
import com._oormthon.seasonthon.domain.s3.dto.PresignedUrlResponse;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.*;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner s3Presigner;
    private final UserService userService;

    @Value("${cloud.s3.bucket}")
    private String bucket;

    /**
     * S3 Presigned URL (PUT) 발급
     */
    public PresignedUrlResponse generateUploadPresignedUrl(Long userId, String fileName, String fileType) {
        UserResponse user = userService.getUserById(userId);

        // 고유한 S3 키 생성 (닉네임/UUID-파일명)
        String key = user.nickname() + "/" + UUID.randomUUID() + "-" + fileName;

        // Presigned URL 발급용 요청 생성
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(fileType)
                .build();

        // Presigned URL 생성
        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(
                r -> r.putObjectRequest(objectRequest)
                        .signatureDuration(Duration.ofMinutes(10)));

        // JSON 형태로 리턴
        return new PresignedUrlResponse(presignedRequest.url().toString(), key);
    }

    /**
     * S3 객체 삭제
     */
    public void deleteFile(Long userId, String fileName) {

        String key = userService.getUserById(userId).nickname() + "/" + fileName;

        S3Client s3 = S3Client.builder()
                .region(Region.AP_NORTHEAST_2)
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .build();

        s3.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
    }

    /**
     * 파일명에서 Content-Type 추론
     */
    // private String determineContentTypeFromKey(String key) {
    // if (key == null)
    // return "application/octet-stream"; // 기본값

    // String lowerKey = key.toLowerCase();

    // if (lowerKey.endsWith(".jpg") || lowerKey.endsWith(".jpeg"))
    // return "image/jpeg";
    // if (lowerKey.endsWith(".png"))
    // return "image/png";
    // if (lowerKey.endsWith(".gif"))
    // return "image/gif";
    // if (lowerKey.endsWith(".webp"))
    // return "image/webp";
    // if (lowerKey.endsWith(".svg"))
    // return "image/svg+xml";

    // return "application/octet-stream"; // 모르는 확장자일 경우
    // }

    /**
     * URL에서 파일명만 추출
     * 예: https://t1.kakaocdn.net/account_images/default_profile.jpeg →
     * default_profile.jpeg
     */
    // private String extractFileNameFromUrl(String url) {
    // if (url == null)
    // return "unknown";
    // int lastSlash = url.lastIndexOf('/');
    // if (lastSlash == -1 || lastSlash == url.length() - 1)
    // return "unknown";
    // return url.substring(lastSlash + 1);
    // }
}
