package com._oormthon.seasonthon.domain.s3.service;

import com._oormthon.seasonthon.domain.member.entity.User;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class S3Service {
    private final AmazonS3 amazonS3;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    public S3Service(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    /**
     * S3에 이미지 업로드 하기
     */
    @Transactional
    public String uploadImage(MultipartFile image, User user) {

        // 기존의 프로필 이미지가 있었다면 삭제
        if (user.getProfileImage() != null)
            amazonS3.deleteObject(bucket, getImageKey(user.getProfileImage()));

        // 파일 확장자 추출
        String extension = getImageExtension(image);
        String fileName = UUID.randomUUID() + "_" + user.getUserId() + "_profile" + extension;

        // 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(image.getContentType());
        metadata.setContentLength(image.getSize());

        // S3에 파일 업로드 요청 생성
        PutObjectRequest putObjectRequest = null;
        try {
            putObjectRequest = new PutObjectRequest(bucket, fileName, image.getInputStream(), metadata);
        } catch (IOException e) {
            log.error(String.valueOf(e.getCause()));
            // throw new S3ImageUploadException("서버오류: 이미지 업로드에 실패하였습니다.");
        }

        // S3에 파일 업로드
        amazonS3.putObject(putObjectRequest);

        // 업로드 후 멤버 객체에 URL 할당
        String publicUrl = getPublicUrl(fileName);
        user.setProfileImage(publicUrl);

        return publicUrl;
    }

    private String getImageExtension(MultipartFile image) {
        String extension = "";
        String originalFilename = image.getOriginalFilename();

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }
        return extension;
    }

    private String getPublicUrl(String imageName) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, amazonS3.getRegionName(), imageName);
    }

    private String getImageKey(String imageUrl) {
        return imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
    }

}