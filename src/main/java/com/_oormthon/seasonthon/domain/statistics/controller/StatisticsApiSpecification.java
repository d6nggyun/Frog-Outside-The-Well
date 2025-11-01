package com._oormthon.seasonthon.domain.statistics.controller;

import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.statistics.dto.res.AchievementRateResponse;
import com._oormthon.seasonthon.domain.statistics.dto.res.FocusTimeResponse;
import com._oormthon.seasonthon.domain.statistics.dto.res.MonthlyTodosResponse;
import com._oormthon.seasonthon.domain.todo.enums.TodoType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.YearMonth;
import java.util.List;

@Tag(name = "Statistics", description = "Statistics 관련 API")
public interface StatisticsApiSpecification {

    @Operation(
            summary = "이번 달 달성 과제 목록 조회",
            description = "yyyy-MM 형식의 year, month 값과 과제 유형(TodoType)을 받아 해당 달에 달성한 과제 목록을 조회합니다." +
                    "<br><br> ex) yearMonth=2025-10, todoType=PREVIEW_REVIEW" +
                    "<br><br> todoType 값: PREVIEW_REVIEW (예습/복습 과제), HOMEWORK (숙제), TEST_STUDY (시험공부), " +
                    "PERFORMANCE_ASSESSMENT (수행평가), CAREER_ACTIVITY (진로활동), ETC (기타)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "이번 달 달성 과제 목록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = MonthlyTodosResponse.class))
                            )
                    )
            }
    )
    ResponseEntity<List<MonthlyTodosResponse>> getTodosMonthly(@AuthenticationPrincipal User user,
                                                               @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth,
                                                               @RequestParam TodoType todoType);

    @Operation(
            summary = "달성률 변화 추이 조회",
            description = "yyyy-MM 형식의 year, month 값을 받아 해당 달에 달성률 변화 추이를 조회합니다." +
                    "<br><br>해당 주차에 값이 존재하지 않으면 rate 값들이 null로 반환됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "달성률 변화 추이 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = AchievementRateResponse.class))
                            )
                    )
            }
    )
    ResponseEntity<List<AchievementRateResponse>> getAchievementRate(@AuthenticationPrincipal User user,
                                                                     @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth);

    @Operation(
            summary = "집중 시간 조회",
            description = "yyyy-MM 형식의 year, month 값을 받아 집중 시간을 조회합니다." +
                    "<br><br>해당 주차에 값이 존재하지 않으면 Duration 값들이 null로 반환됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "집중 시간 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = FocusTimeResponse.class))
                            )
                    )
            }
    )
    ResponseEntity<List<FocusTimeResponse>> getFocusTime(@AuthenticationPrincipal User user,
                                                         @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth);
}
