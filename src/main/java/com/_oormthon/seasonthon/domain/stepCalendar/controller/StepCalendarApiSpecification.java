package com._oormthon.seasonthon.domain.stepCalendar.controller;

import com._oormthon.seasonthon.domain.stepCalendar.dto.res.ListStepCalendarResponse;
import com._oormthon.seasonthon.domain.member.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "StepCalendar", description = "캘린더 관련 API")
public interface StepCalendarApiSpecification {

    @Operation(
            summary = "캘린더 조회",
            description = "캘린더를 년 / 월 단위로 조회합니다. <br><br> " +
                    "calendar: 해당 월의 모든 캘린더 일정 <br>" +
                    "todayToDo: 오늘의 할 일",
            responses = {
                    @ApiResponse(responseCode = "200", description = "캘린더 조회",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = ListStepCalendarResponse.class))
                            )
                    ),
            }
    )
    ResponseEntity<ListStepCalendarResponse> findTodoCalendar(@AuthenticationPrincipal User user,
                                                              @RequestParam int year, @RequestParam int month);
}
