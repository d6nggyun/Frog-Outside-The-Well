package com._oormthon.seasonthon.domain.step.controller;

import com._oormthon.seasonthon.domain.step.dto.req.UpdateStepRequest;
import com._oormthon.seasonthon.domain.step.dto.res.StepRecordResponse;
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
                    )
            }
    )
    ResponseEntity<TodoStepResponse> getTodoSteps(@PathVariable Long todoId);

    @Operation(
            summary = "Step 기록 시작",
            description = "해당 Step의 기록을 시작합니다. <br><br>시작 시간을 기록하며, 시작 시 완료됨으로 표시합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Step 기록 시작",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = StepRecordResponse.class)
                            )
                    )
            }
    )
    ResponseEntity<StepRecordResponse> startStep(@PathVariable Long stepId);

    @Operation(
            summary = "Step 기록 종료",
            description = "해당 Step의 기록을 종료합니다. <br><br>종료 시간을 기록하며, 종료 시 저장된 시작 시간을 기준으로 총 소요시간을 누적합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Step 기록 종료",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = StepRecordResponse.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "시작되지 않은 Step입니다.",
                            content = @Content(schema = @Schema(implementation = ErrorResponseEntity.class),
                                    examples = @ExampleObject(
                                            value = """
                                                {
                                                   "code": 404,
                                                   "name": "STEP_NOT_STARTED",
                                                   "message": "시작되지 않은 Step입니다.",
                                                   "errors": null
                                                }
                                                """
                                    )
                            )
                    )
            }
    )
    ResponseEntity<StepRecordResponse> stopStep(@PathVariable Long stepId);

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
