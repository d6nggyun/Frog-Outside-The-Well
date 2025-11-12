package com._oormthon.seasonthon.domain.s3.controller;

import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.s3.dto.PresignedUrlResponse;
import com._oormthon.seasonthon.global.exception.ErrorResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "S3", description = "AWS S3 관련 API")
public interface S3ApiSpecification {

    @Operation(summary = "Presigned URL 발급 (이미지 업로드용)", description = """
            S3에 직접 파일을 업로드하기 위한 Presigned URL을 발급합니다.<br><br>
            프론트엔드에서 이 URL로 파일을 업로드할 수 있습니다.<br><br>
            파일명과 MIME 타입(fileType)을 함께 전달해야 합니다.
            """, responses = {
            @ApiResponse(responseCode = "200", description = "Presigned URL 발급 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PresignedUrlResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 파라미터 오류", content = @Content(schema = @Schema(implementation = ErrorResponseEntity.class), examples = @ExampleObject(value = """
                    {
                      "code": 400,
                      "name": "INVALID_REQUEST",
                      "message": "파일명 또는 파일 타입이 유효하지 않습니다.",
                      "errors": null
                    }
                    """)))
    })
    ResponseEntity<PresignedUrlResponse> getPresignedUploadUrl(
            @AuthenticationPrincipal User user,
            @RequestParam String fileName,
            @RequestParam String fileType);

    @Operation(summary = "Presigned URL 발급 (이미지 업로드용)", description = """
            S3에 직접 파일을 업로드하기 위한 Presigned URL을 발급합니다.<br><br>
            프론트엔드에서 이 URL로 파일을 업로드할 수 있습니다.<br><br>
            fileType은 파일명으로 정해집니다.
            """, responses = {
            @ApiResponse(responseCode = "200", description = "Presigned URL 발급 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PresignedUrlResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 파라미터 오류", content = @Content(schema = @Schema(implementation = ErrorResponseEntity.class), examples = @ExampleObject(value = """
                    {
                      "code": 400,
                      "name": "INVALID_REQUEST",
                      "message": "파일명 또는 파일 타입이 유효하지 않습니다.",
                      "errors": null
                    }
                    """)))
    })
    ResponseEntity<PresignedUrlResponse> getPresignedUploadUrlWithoutFileType(
            @AuthenticationPrincipal User user,
            @RequestParam String fileName);

    @Operation(summary = "S3 파일 삭제", description = """
            S3에 업로드된 이미지를 삭제합니다.<br><br>
            서버 측에서 직접 삭제 요청을 수행합니다.
            """, responses = {
            @ApiResponse(responseCode = "204", description = "파일 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "파일을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ErrorResponseEntity.class), examples = @ExampleObject(value = """
                    {
                      "code": 404,
                      "name": "FILE_NOT_FOUND",
                      "message": "삭제할 파일을 찾을 수 없습니다.",
                      "errors": null
                    }
                    """)))
    })
    ResponseEntity<Void> deleteImage(
            @AuthenticationPrincipal User user,
            @RequestParam String fileName);
}
