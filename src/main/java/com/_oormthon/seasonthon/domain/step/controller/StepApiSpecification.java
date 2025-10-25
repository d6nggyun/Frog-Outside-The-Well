package com._oormthon.seasonthon.domain.step.controller;

import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.step.dto.req.UpdateStepRequest;
import com._oormthon.seasonthon.domain.step.dto.res.OneStepResponse;
import com._oormthon.seasonthon.domain.step.dto.res.StepResponse;
import com._oormthon.seasonthon.domain.todo.dto.res.TodoStepResponse;
import com._oormthon.seasonthon.global.exception.ErrorResponseEntity;
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

@Tag(name = "Step", description = "Step 관련 API")
public interface StepApiSpecification {

    @Operation(
            summary = "ToDo의 Step 목록 조회",
            description = "ToDo Id 값을 기반으로 ToDo의 Step 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "ToDo의 Step 목록 조회",
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
                    ),
                    @ApiResponse(responseCode = "403", description = "ToDo에 접근할 권한이 없습니다.",
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
    ResponseEntity<TodoStepResponse> getTodoSteps(@AuthenticationPrincipal User user,
                                                  @PathVariable Long todoId);

    @Operation(
            summary = "오늘의 한 걸음 / 놓친 한 걸음 조회",
            description = "오늘의 한 걸음, 놓친 한 걸음 리스트를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "오늘의 한 걸음 / 놓친 한 걸음 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OneStepResponse.class)
                            )
                    )
            }
    )
    ResponseEntity<OneStepResponse> getOneSteps(@AuthenticationPrincipal User user);

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
                    @ApiResponse(responseCode = "403", description = "Step에 접근할 권한이 없습니다.",
                            content = @Content(schema = @Schema(implementation = ErrorResponseEntity.class),
                                    examples = @ExampleObject(
                                            value = """
                                                {
                                                   "code": 403,
                                                   "name": "STEP_ACCESS_DENIED",
                                                   "message": "Step에 접근할 권한이 없습니다.",
                                                   "errors": null
                                                }
                                                """
                                    )
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
    ResponseEntity<List<StepResponse>> updateStep(@AuthenticationPrincipal User user,
                                                  @PathVariable Long stepId,
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
                    @ApiResponse(responseCode = "403", description = "Step에 접근할 권한이 없습니다.",
                            content = @Content(schema = @Schema(implementation = ErrorResponseEntity.class),
                                    examples = @ExampleObject(
                                            value = """
                                                {
                                                   "code": 403,
                                                   "name": "STEP_ACCESS_DENIED",
                                                   "message": "Step에 접근할 권한이 없습니다.",
                                                   "errors": null
                                                }
                                                """
                                    )
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
    ResponseEntity<List<StepResponse>> deleteStep(@AuthenticationPrincipal User user,
                                                  @PathVariable Long stepId);
}
