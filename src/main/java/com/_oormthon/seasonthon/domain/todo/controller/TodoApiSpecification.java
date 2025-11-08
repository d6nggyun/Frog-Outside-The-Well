package com._oormthon.seasonthon.domain.todo.controller;

import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.todo.dto.req.TodoRequest;
import com._oormthon.seasonthon.domain.todo.dto.req.UpdateTodoDetailRequest;
import com._oormthon.seasonthon.domain.todo.dto.req.UpdateTodoRequest;
import com._oormthon.seasonthon.domain.todo.dto.res.TodoResponse;
import com._oormthon.seasonthon.global.exception.ErrorResponseEntity;
import com._oormthon.seasonthon.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "ToDo", description = "ToDo 관련 API")
public interface TodoApiSpecification {

        @Operation(summary = "회원의 ToDo 조회", description = "회원이 등록한 모든 ToDo 리스트를 조회합니다. <br><br>진행률(progress)는 퍼센트를 기반으로 반환됩니다."
                        +
                        "<br><br>todoType 값: PREVIEW_REVIEW (예습/복습 과제), HOMEWORK (숙제), TEST_STUDY (시험공부), " +
                        "<br><br>PERFORMANCE_ASSESSMENT (수행평가), CAREER_ACTIVITY (진로활동), ETC (기타)", responses = {
                                        @ApiResponse(responseCode = "200", description = "회원의 ToDo 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class))),
                        })
        ResponseEntity<PageResponse<TodoResponse>> findTodos(@AuthenticationPrincipal User user);

        @Operation(summary = "ToDo 추가", description = "회원이 자신의 ToDo 업무를 추가합니다. <br><br>Step까지 한꺼번에 요청 값으로 보내주세요." +
                        "<br><br>todoType 값: PREVIEW_REVIEW (예습/복습 과제), HOMEWORK (숙제), TEST_STUDY (시험공부)," +
                        "PERFORMANCE_ASSESSMENT (수행평가), CAREER_ACTIVITY (진로활동), ETC (기타)", responses = {
                                        @ApiResponse(responseCode = "200", description = "ToDo 추가", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TodoResponse.class))),
                        })
        ResponseEntity<TodoResponse> addTodo(@AuthenticationPrincipal User user,
                        @Valid @RequestBody TodoRequest todoRequest);

        @Operation(summary = "ToDo 삭제", description = "회원이 자신의 ToDo 업무를 삭제합니다.", responses = {
                        @ApiResponse(responseCode = "200", description = "ToDo 삭제", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Void.class))),
        })
        ResponseEntity<Void> deleteTodo(@AuthenticationPrincipal User user,
                        @PathVariable Long todoId);

        @Operation(summary = "ToDo 완료", description = "ToDo Id 값을 기반으로 ToDo를 완료 처리합니다. <br><br>해당 ToDo의 진행률을 100%로 업데이트합니다.", responses = {
                        @ApiResponse(responseCode = "200", description = "ToDo 완료", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TodoResponse.class))),
                        @ApiResponse(responseCode = "403", description = "ToDo에 접근할 권한이 없는 회원", content = @Content(schema = @Schema(implementation = ErrorResponseEntity.class), examples = @ExampleObject(value = """
                                        {
                                           "code": 403,
                                           "name": "TODO_ACCESS_DENIED",
                                           "message": "ToDo에 접근할 권한이 없습니다.",
                                           "errors": null
                                        }
                                        """)))
        })
        ResponseEntity<TodoResponse> completeTodo(@AuthenticationPrincipal User user,
                        @PathVariable Long todoId);

        @Operation(summary = "ToDo 목표 재설정", description = "ToDo Id 값을 기반으로 ToDo의 목표를 재설정합니다.", responses = {
                        @ApiResponse(responseCode = "200", description = "ToDo 목표 재설정", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TodoResponse.class))),
                        @ApiResponse(responseCode = "403", description = "ToDo에 접근할 권한이 없는 회원", content = @Content(schema = @Schema(implementation = ErrorResponseEntity.class), examples = @ExampleObject(value = """
                                        {
                                           "code": 403,
                                           "name": "TODO_ACCESS_DENIED",
                                           "message": "ToDo에 접근할 권한이 없습니다.",
                                           "errors": null
                                        }
                                        """)))
        })
        ResponseEntity<TodoResponse> updateTodo(@AuthenticationPrincipal User user,
                        @PathVariable Long todoId,
                        @Valid @RequestBody UpdateTodoRequest updateTodoRequest);

        @Operation(summary = "ToDo 제목 및 유형 수정", description = """
                        ToDo Id를 기반으로 해당 업무의 제목(title)과 유형(todoType)을 수정합니다. <br><br>
                        todoType 값: <br>
                        PREVIEW_REVIEW (예습/복습 과제), HOMEWORK (숙제), TEST_STUDY (시험공부),<br>
                        PERFORMANCE_ASSESSMENT (수행평가), CAREER_ACTIVITY (진로활동), ETC (기타)
                        """, responses = {
                        @ApiResponse(responseCode = "200", description = "ToDo 제목 및 유형 수정 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TodoResponse.class))),
                        @ApiResponse(responseCode = "400", description = "유효하지 않은 요청 값", content = @Content(schema = @Schema(implementation = ErrorResponseEntity.class), examples = @ExampleObject(value = """
                                        {
                                           "code": 400,
                                           "name": "INVALID_REQUEST",
                                           "message": "업무명 또는 ToDo 유형이 비어있습니다.",
                                           "errors": [
                                             {"field": "title", "reason": "must not be blank"},
                                             {"field": "todoType", "reason": "must not be blank"}
                                           ]
                                        }
                                        """))),
                        @ApiResponse(responseCode = "403", description = "ToDo에 접근할 권한이 없는 회원", content = @Content(schema = @Schema(implementation = ErrorResponseEntity.class), examples = @ExampleObject(value = """
                                        {
                                           "code": 403,
                                           "name": "TODO_ACCESS_DENIED",
                                           "message": "ToDo에 접근할 권한이 없습니다.",
                                           "errors": null
                                        }
                                        """)))
        })
        ResponseEntity<TodoResponse> updateTodoTitleType(
                        @AuthenticationPrincipal User user,
                        @PathVariable Long todoId,
                        @Valid @RequestBody UpdateTodoDetailRequest request);

}
