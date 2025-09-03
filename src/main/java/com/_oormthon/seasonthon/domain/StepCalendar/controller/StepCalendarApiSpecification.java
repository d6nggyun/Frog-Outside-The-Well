package com._oormthon.seasonthon.domain.StepCalendar.controller;

import com._oormthon.seasonthon.domain.StepCalendar.dto.req.StepCalendarRequest;
import com._oormthon.seasonthon.domain.StepCalendar.dto.res.StepCalendarResponse;
import com._oormthon.seasonthon.domain.member.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "StepCalendar", description = "캘린더 관련 API")
public interface StepCalendarApiSpecification {

    @Operation(
            summary = "캘린더 조회",
            description = "캘린더를 년 / 월 단위로 조회합니다. <br><br>반한된 날짜에 대한 ToDo 실행 수를 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "캘린더 조회",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = StepCalendarResponse.class))
                            )
                    ),
            }
    )
    ResponseEntity<List<StepCalendarResponse>> findTodoCalendar(@AuthenticationPrincipal User user,
                                                                @Valid @RequestBody StepCalendarRequest request);
}
