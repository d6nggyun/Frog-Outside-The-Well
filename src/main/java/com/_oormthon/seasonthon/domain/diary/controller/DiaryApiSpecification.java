package com._oormthon.seasonthon.domain.diary.controller;

import com._oormthon.seasonthon.domain.diary.dto.res.DiaryResponse;
import com._oormthon.seasonthon.domain.member.entity.User;
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

@Tag(name = "Diary", description = "Diary 관련 API")
public interface DiaryApiSpecification {

    @Operation(
            summary = "특정 달의 Diary 조회",
            description = "yyyy-MM 형식의 year, month 값을 기반으로 해당 달의 Diary 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "특정 달의 Diary 조회",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = DiaryResponse.class))
                            )
                    ),
            }
    )
    ResponseEntity<List<DiaryResponse>> findDiaries(@AuthenticationPrincipal User user,
                                                    @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth);
}
