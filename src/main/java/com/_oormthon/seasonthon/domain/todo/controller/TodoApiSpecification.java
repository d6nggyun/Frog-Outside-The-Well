package com._oormthon.seasonthon.domain.todo.controller;

import com._oormthon.seasonthon.domain.member.domain.Member;
import com._oormthon.seasonthon.domain.todo.dto.res.TodoResponse;
import com._oormthon.seasonthon.domain.todo.dto.res.TodoStepResponse;
import com._oormthon.seasonthon.global.exception.ErrorResponseEntity;
import com._oormthon.seasonthon.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;

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

    @Operation(
            summary = "ToDo의 스텝 목록 조회",
            description = "ToDo Id 값을 기반으로 ToDo의 스텝 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "ToDo의 스텝 목록 조회",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TodoStepResponse.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "ToDo를 찾을 수 없습니다.",
                            content = @Content(schema = @Schema(implementation = ErrorResponseEntity.class),
                                    examples = @ExampleObject(
                                            value = """
                                                {
                                                   "code": 404,
                                                   "name": "TODO_NOT_FOUND",
                                                   "message": "ToDo를 찾을 수 없습니다.",
                                                   "errors": null
                                                }
                                                """
                                    )
                            )
                    )
            }
    )
    ResponseEntity<TodoStepResponse> getTodoSteps(@PathVariable Long todoId);
}
