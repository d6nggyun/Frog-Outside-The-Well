package com._oormthon.seasonthon.domain.stepRecord.controller;

import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.stepRecord.dto.req.StepStartRequest;
import com._oormthon.seasonthon.domain.stepRecord.dto.req.StepStopRequest;
import com._oormthon.seasonthon.domain.stepRecord.dto.res.StepRecordResponse;
import com._oormthon.seasonthon.global.exception.ErrorResponseEntity;
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

@Tag(name = "Step Record", description = "Step Record 관련 API")
public interface StepRecordApiSpecification {

    @Operation(
            summary = "Step 기록 시작",
            description = "해당 Step의 기록을 시작합니다. " +
                    "<br>시작 시간을 body에 담아 보내주세요." +
                    "<br><br>시작 시간을 기록합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Step 기록 시작",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = StepRecordResponse.class)
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
                    )
            }
    )
    ResponseEntity<StepRecordResponse> startStep(@AuthenticationPrincipal User user,
                                                 @PathVariable Long stepId,
                                                 @Valid @RequestBody StepStartRequest request);

    @Operation(
            summary = "Step 기록 일시정지",
            description = "해당 Step의 기록을 일시정지합니다. 휴식 카운트를 증가합니다." +
                    "<br> 일시정지 시간과, 수행한 시간을 body에 담아 보내주세요." +
                    "<br><br>종료 시간을 기록하고, 수행한 시간을 기존의 수행 시간에 더합니다. " +
                    "<br>ex) 1분 수행 후 일시정지 (1분 전송) -> 2분 수행 후 일시정지 (2분 전송) = 총 3분 수행 기록",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Step 기록 시작",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = StepRecordResponse.class)
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
                    )
            }
    )
    ResponseEntity<StepRecordResponse> pauseStep(@AuthenticationPrincipal User user,
                                                 @PathVariable Long stepId,
                                                 @Valid @RequestBody StepStopRequest request);

    @Operation(
            summary = "Step 기록 종료",
            description = "해당 Step의 기록을 종료합니다. 해당 Step을 완료 처리하고 캘린더에 기록합니다." +
                    "<br> 종료 시간과, 수행한 시간을 body에 담아 보내주세요." +
                    "<br><br>종료 시간을 기록하고, 수행한 시간을 기존의 수행 시간에 더합니다. " +
                    "<br>ex) 1분 수행 후 일시정지 (1분 전송) -> 2분 수행 후 종료 (2분 전송) = 총 3분 수행 기록",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Step 기록 종료",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = StepRecordResponse.class)
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
    ResponseEntity<StepRecordResponse> stopStep(@AuthenticationPrincipal User user,
                                                @PathVariable Long stepId,
                                                @Valid @RequestBody StepStopRequest request);
}
