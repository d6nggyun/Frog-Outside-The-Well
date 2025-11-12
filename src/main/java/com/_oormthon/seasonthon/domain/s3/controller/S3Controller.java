package com._oormthon.seasonthon.domain.s3.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com._oormthon.seasonthon.domain.member.entity.User;
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
    @GetMapping("/presigned-upload")
    public ResponseEntity<String> getPresignedUploadUrl(
            @AuthenticationPrincipal User user) {
        String presignedUrl = s3Service.generateUploadPresignedUrl(user.getUserId());
        return ResponseEntity.ok(presignedUrl);
    }

    /**
     * 이미지 삭제 (서버가 직접 삭제 요청)
     */
    @DeleteMapping("/file")
    public ResponseEntity<Void> deleteImage(@RequestParam Long userId) {
        s3Service.deleteFile(userId);
        return ResponseEntity.noContent().build();
    }
}