package com._oormthon.seasonthon.domain.diary.controller;

import com._oormthon.seasonthon.domain.diary.dto.res.DiaryDetailResponse;
import com._oormthon.seasonthon.domain.diary.dto.res.DiaryResponse;
import com._oormthon.seasonthon.domain.diary.service.DiaryService;
import com._oormthon.seasonthon.domain.member.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/diaries")
public class DiaryController implements DiaryApiSpecification {

    private final DiaryService diaryService;

    @GetMapping
    public ResponseEntity<List<DiaryResponse>> findDiaries(@AuthenticationPrincipal User user,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) {
        return ResponseEntity.status(HttpStatus.OK).body(diaryService.findDiaries(user, yearMonth));
    }

    @GetMapping("/detail")
    public ResponseEntity<DiaryDetailResponse> getDiaryDetail(@AuthenticationPrincipal User user,
            @RequestParam LocalDate date) {
        return ResponseEntity.status(HttpStatus.OK).body(diaryService.getDiaryDetail(user, date));
    }
}
