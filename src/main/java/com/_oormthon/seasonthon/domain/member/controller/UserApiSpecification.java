package com._oormthon.seasonthon.domain.member.controller;

import com._oormthon.seasonthon.domain.member.dto.res.UserResponse;
import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.global.exception.ErrorResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "User", description = "User 관련 API")
public interface UserApiSpecification {

    @Operation(
            summary = "회원 정보 조회",
            description = "로그인한 사용자의 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "회원 정보 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UserResponse.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없습니다.",
                            content = @Content(schema = @Schema(implementation = ErrorResponseEntity.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                       "code": 404,
                                                       "name": "MEMBER_NOT_FOUND",
                                                       "message": "회원을 찾을 수 없습니다.",
                                                       "errors": null
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    ResponseEntity<UserResponse> getMyPage(@AuthenticationPrincipal User user);

    @Operation(
            summary = "회원 정보 수정",
            description = "로그인한 사용자의 정보를 수정합니다. <br><br>" +
                    "입력되지 않은 필드는 기존 값이 유지됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "회원 정보 수정 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UserResponse.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없습니다.",
                            content = @Content(schema = @Schema(implementation = ErrorResponseEntity.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                       "code": 404,
                                                       "name": "MEMBER_NOT_FOUND",
                                                       "message": "회원을 찾을 수 없습니다.",
                                                       "errors": null
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    ResponseEntity<UserResponse> updateMyPage(@AuthenticationPrincipal User user,
                                              @RequestBody UserResponse request);
}
