package com._oormthon.seasonthon.domain.member.controller;

import com._oormthon.seasonthon.domain.member.dto.request.DailyLogBeforeRequest;
import com._oormthon.seasonthon.domain.member.dto.request.DailyLogAfterRequest;
import com._oormthon.seasonthon.domain.member.dto.response.DailyLogBeforeResponse;
import com._oormthon.seasonthon.domain.member.dto.response.DailyLogAfterResponse;
import com._oormthon.seasonthon.domain.member.enums.PlaceType;
import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.global.exception.ErrorResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.Map;

@Tag(name = "Daily Log", description = "Daily Log 관련 API ")
public interface DailyLogApiSpecification {

    // ===== DailyLogBefore =====
    @Operation(summary = "오늘 DailyLogBefore 생성", description = "사용자의 DailyLogBefore를 생성합니다.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "DailyLogBefore 생성 완료", content = @Content(schema = @Schema(implementation = DailyLogBeforeResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한이 없습니다.", content = @Content(schema = @Schema(implementation = ErrorResponseEntity.class), examples = @ExampleObject(value = "{\"code\":403,\"name\":\"ACCESS_DENIED\",\"message\":\"권한이 없습니다.\",\"errors\":null}")))
    })
    ResponseEntity<DailyLogBeforeResponse> createBefore(@AuthenticationPrincipal User user,
            @Valid DailyLogBeforeRequest request);

    @Operation(summary = "오늘 DailyLogBefore 조회", description = "사용자의 오늘 DailyLogBefore를 조회합니다.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "DailyLogBefore 조회", content = @Content(schema = @Schema(implementation = DailyLogBeforeResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "오늘 기록이 없습니다.", content = @Content(schema = @Schema(implementation = ErrorResponseEntity.class)))
    })
    ResponseEntity<DailyLogBeforeResponse> getTodayBefore(@AuthenticationPrincipal User user);

    @Operation(summary = "이번 주 PlaceType 통계", description = "사용자의 이번 주 DailyLogBefore 장소별 합계를 조회합니다.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "PlaceType 합계 조회")
    })
    ResponseEntity<Map<PlaceType, Long>> getThisWeekPlaceType(@AuthenticationPrincipal User user);

    @Operation(summary = "이번 달 PlaceType 통계", description = "사용자의 이번 달 DailyLogBefore 장소별 합계를 조회합니다.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "PlaceType 합계 조회")
    })
    ResponseEntity<Map<PlaceType, Long>> getThisMonthPlaceType(@AuthenticationPrincipal User user);

    // ===== DailyLogAfter =====
    @Operation(summary = "오늘 DailyLogAfter 생성", description = "사용자의 DailyLogAfter를 생성합니다.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "DailyLogAfter 생성 완료", content = @Content(schema = @Schema(implementation = DailyLogAfterResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한이 없습니다.", content = @Content(schema = @Schema(implementation = ErrorResponseEntity.class)))
    })
    ResponseEntity<DailyLogAfterResponse> createAfter(@AuthenticationPrincipal User user,
            @Valid DailyLogAfterRequest request);

    @Operation(summary = "오늘 DailyLogAfter 조회", description = "사용자의 오늘 DailyLogAfter를 조회합니다.", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "DailyLogAfter 조회", content = @Content(schema = @Schema(implementation = DailyLogAfterResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "오늘 기록이 없습니다.", content = @Content(schema = @Schema(implementation = ErrorResponseEntity.class)))
    })
    ResponseEntity<DailyLogAfterResponse> getTodayAfter(@AuthenticationPrincipal User user);
}
