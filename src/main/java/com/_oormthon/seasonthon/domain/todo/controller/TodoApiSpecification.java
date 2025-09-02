package com._oormthon.seasonthon.domain.todo.controller;

import com._oormthon.seasonthon.domain.member.domain.Member;
import com._oormthon.seasonthon.domain.todo.dto.res.TodoResponse;
import com._oormthon.seasonthon.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "ToDo", description = "ToDo 관련 API")
public interface TodoApiSpecification {

    @Operation(
            summary = "회원의 ToDo 조회",
            description = "회원이 등록한 모든 ToDo 리스트를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "회원의 ToDo 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PageResponse.class)
                            )
                    ),
            }
    )
    ResponseEntity<PageResponse<TodoResponse>> findTodos(@AuthenticationPrincipal Member member);


}
