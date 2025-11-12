package com._oormthon.seasonthon.domain.s3.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.s3.dto.PresignedUrlResponse;
import com._oormthon.seasonthon.domain.s3.service.S3Service;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/s3")
public class S3Controller {

    private final S3Service s3Service;

    /**
     * 업로드용 Presigned URL 발급
     * 프론트에서 파일 업로드 전에 요청
     */
    @PostMapping("/presigned")
    public ResponseEntity<PresignedUrlResponse> getPresignedUploadUrl(
            @AuthenticationPrincipal User user,
            @RequestParam String fileName,
            @RequestParam String fileType) {

        PresignedUrlResponse response = s3Service.generateUploadPresignedUrl(user.getUserId(), fileName, fileType);
        return ResponseEntity.ok(response);
    }

    /**
     * 이미지 삭제 (서버가 직접 삭제 요청)
     */
    @DeleteMapping("/file")
    public ResponseEntity<Void> deleteImage(@AuthenticationPrincipal User user, @RequestParam String fileName) {
        s3Service.deleteFile(user.getUserId(), fileName);
        return ResponseEntity.noContent().build();
    }
}