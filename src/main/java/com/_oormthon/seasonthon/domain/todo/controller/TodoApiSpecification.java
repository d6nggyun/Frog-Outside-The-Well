package com._oormthon.seasonthon.domain.todo.controller;

import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.todo.dto.req.TodoRequest;
import com._oormthon.seasonthon.domain.todo.dto.req.UpdateStepRequest;
import com._oormthon.seasonthon.domain.todo.dto.req.UpdateTodoRequest;
import com._oormthon.seasonthon.domain.todo.dto.res.StepResponse;
import com._oormthon.seasonthon.domain.todo.dto.res.TodoResponse;
import com._oormthon.seasonthon.domain.todo.dto.res.TodoStepResponse;
import com._oormthon.seasonthon.global.exception.ErrorResponseEntity;
import com._oormthon.seasonthon.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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

import java.util.List;

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
    ResponseEntity<PageResponse<TodoResponse>> findTodos(@AuthenticationPrincipal User user);

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

    @Operation(
            summary = "ToDo 추가",
            description = "회원이 자신의 ToDo 업무를 추가합니다. <br><br>Step까지 한꺼번에 요청 값으로 보내주세요.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "ToDo 추가",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TodoResponse.class)
                            )
                    ),
            }
    )
    ResponseEntity<TodoResponse> addTodo(@AuthenticationPrincipal User user,
                                         @Valid @RequestBody TodoRequest todoRequest);

    @Operation(
            summary = "ToDo 목표 재설정",
            description = "ToDo Id 값을 기반으로 ToDo의 목표를 재설정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "ToDo 목표 재설정",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TodoResponse.class)
                            )
                    ),
                    @ApiResponse(responseCode = "403", description = "ToDo에 접근할 권한이 없는 회원",
                            content = @Content(schema = @Schema(implementation = ErrorResponseEntity.class),
                                    examples = @ExampleObject(
                                            value = """
                                                {
                                                   "code": 403,
                                                   "name": "TODO_ACCESS_DENIED",
                                                   "message": "ToDo에 접근할 권한이 없습니다.",
                                                   "errors": null
                                                }
                                                """
                                    )
                            )
                    )
            }
    )
    ResponseEntity<TodoResponse> updateTodo(@AuthenticationPrincipal User user,
                                            @PathVariable Long todoId,
                                            @Valid @RequestBody UpdateTodoRequest updateTodoRequest);

    @Operation(
            summary = "(미구현) 캘린더 해당 달 조회",
            description = "",
            responses = {
                    @ApiResponse(responseCode = "200", description = "캘린더 해당 달 조회",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Object.class)
                            )
                    ),
            }
    )
    ResponseEntity<Object> findTodoCalendar();

    @Operation(
            summary = "Step 완료",
            description = "Step Id 값을 기반으로 Step을 완료합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Step 완료",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = StepResponse.class))
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Step을 찾을 수 없습니다.",
                            content = @Content(schema = @Schema(implementation = ErrorResponseEntity.class),
                                    examples = @ExampleObject(
                                            value = """
                                                {
                                                   "code": 404,
                                                   "name": "STEP_NOT_FOUND",
                                                   "message": "Step을 찾을 수 없습니다..",
                                                   "errors": null
                                                }
                                                """
                                    )
                            )
                    )
            }
    )
    ResponseEntity<List<StepResponse>> completeStep(@PathVariable Long stepId);

    @Operation(
            summary = "Step 수정",
            description = "Step Id 값을 기반으로 Step을 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Step 수정",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = StepResponse.class))
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Step을 찾을 수 없습니다.",
                            content = @Content(schema = @Schema(implementation = ErrorResponseEntity.class),
                                    examples = @ExampleObject(
                                            value = """
                                                {
                                                   "code": 404,
                                                   "name": "STEP_NOT_FOUND",
                                                   "message": "Step을 찾을 수 없습니다..",
                                                   "errors": null
                                                }
                                                """
                                    )
                            )
                    )
            }
    )
    ResponseEntity<List<StepResponse>> updateStep(@PathVariable Long stepId,
                                                         @Valid @RequestBody UpdateStepRequest updateStepRequest);

    @Operation(
            summary = "Step 삭제",
            description = "Step Id 값을 기반으로 Step을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Step 삭제",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = StepResponse.class))
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Step을 찾을 수 없습니다.",
                            content = @Content(schema = @Schema(implementation = ErrorResponseEntity.class),
                                    examples = @ExampleObject(
                                            value = """
                                                {
                                                   "code": 404,
                                                   "name": "STEP_NOT_FOUND",
                                                   "message": "Step을 찾을 수 없습니다..",
                                                   "errors": null
                                                }
                                                """
                                    )
                            )
                    )
            }
    )
    ResponseEntity<List<StepResponse>> deleteStep(@PathVariable Long stepId);
}
