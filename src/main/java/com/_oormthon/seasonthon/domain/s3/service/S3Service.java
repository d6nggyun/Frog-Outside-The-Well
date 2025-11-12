package com._oormthon.seasonthon.domain.s3.service;

import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.global.exception.S3ImageException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
public class S3Service {
    private final AmazonS3 amazonS3;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    public S3Service(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    /**
     * S3에 이미지 업로드 하기
     */
    @Transactional
    public String uploadImage(MultipartFile image, User user) {
        if (user.getProfileImage() != null) {
            try {
                amazonS3.deleteObject(bucket, getImageKey(user.getProfileImage()));
            } catch (Exception e) {
                log.warn("기존 프로필 이미지를 삭제하지 못했습니다: {}", e.getMessage());
            }
        }

        String extension = getImageExtension(image);
        String fileName = UUID.randomUUID() + "_" + user.getUserId() + "_profile" + extension;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(image.getContentType());
        metadata.setContentLength(image.getSize());

        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, fileName, image.getInputStream(),
                    metadata);
            amazonS3.putObject(putObjectRequest);
        } catch (IOException e) {
            log.error("이미지 업로드 실패: {}", e.getMessage(), e);
            throw new S3ImageException("이미지 업로드 중 오류가 발생했습니다: " + e.getMessage(), e);
        }

        String publicUrl = getPublicUrl(fileName);
        user.setProfileImage(publicUrl);
        return publicUrl;
    }

    /**
     * S3에서 이미지 다운로드드
     */
    @Transactional(readOnly = true)
    public S3Object getFile(String keyName) {
        try {
            log.info("S3에서 파일 다운로드 시도: {}", keyName);

            if (!amazonS3.doesObjectExist(bucket, keyName)) {
                log.error("S3에서 파일을 찾을 수 없습니다: {}", keyName);
                throw new S3ImageException("존재하지 않는 파일입니다: " + keyName);
            }

            S3Object s3Object = amazonS3.getObject(new GetObjectRequest(bucket, keyName));
            log.info("S3 파일 다운로드 성공: {}", keyName);
            return s3Object;

        } catch (S3ImageException e) {
            throw e;
        } catch (Exception e) {
            log.error("S3 파일 다운로드 중 오류 발생: {}", e.getMessage(), e);
            throw new S3ImageException("이미지 다운로드 중 오류가 발생했습니다.", e);
        }
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
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, imageName);
    }

    private String getImageKey(String imageUrl) {
        return imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
    }

}