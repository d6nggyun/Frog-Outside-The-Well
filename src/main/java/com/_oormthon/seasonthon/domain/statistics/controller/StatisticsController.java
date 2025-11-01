package com._oormthon.seasonthon.domain.statistics.controller;

import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.statistics.dto.res.AchievementRateResponse;
import com._oormthon.seasonthon.domain.statistics.dto.res.FocusTimeResponse;
import com._oormthon.seasonthon.domain.statistics.dto.res.MonthlyTodosResponse;
import com._oormthon.seasonthon.domain.statistics.service.StatisticsService;
import com._oormthon.seasonthon.domain.todo.enums.TodoType;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/statistics")
public class StatisticsController implements StatisticsApiSpecification {

    private final StatisticsService statisticsService;

    @GetMapping("/todos/monthly")
    public ResponseEntity<List<MonthlyTodosResponse>> getTodosMonthly(@AuthenticationPrincipal User user,
                                                                      @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth,
                                                                      @RequestParam TodoType todoType) {
        return ResponseEntity.status(HttpStatus.OK).body(statisticsService.getTodosMonthly(user, yearMonth, todoType));
    }

    @GetMapping("/achievement-rate")
    public ResponseEntity<List<AchievementRateResponse>> getAchievementRate(@AuthenticationPrincipal User user,
                                                                            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) {
        return ResponseEntity.status(HttpStatus.OK).body(statisticsService.getAchievementRate(user, yearMonth));
    }

    @GetMapping("/focus-time")
    public ResponseEntity<List<FocusTimeResponse>> getFocusTime(@AuthenticationPrincipal User user,
                                                                @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) {
        return ResponseEntity.status(HttpStatus.OK).body(statisticsService.getFocusTime(user, yearMonth));
    }
}
