package com._oormthon.seasonthon.domain.StepCalendar.controller;

import com._oormthon.seasonthon.domain.StepCalendar.dto.req.StepCalendarRequest;
import com._oormthon.seasonthon.domain.StepCalendar.dto.res.StepCalendarResponse;
import com._oormthon.seasonthon.domain.StepCalendar.service.StepCalendarService;
import com._oormthon.seasonthon.domain.member.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/steps")
public class StepCalendarController implements StepCalendarApiSpecification{

    private final StepCalendarService stepCalendarService;

    // 캘린더 조회
    @GetMapping("/calendar")
    public ResponseEntity<List<StepCalendarResponse>> findTodoCalendar(@AuthenticationPrincipal User user,
                                                 @Valid @RequestBody StepCalendarRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(stepCalendarService.findTodoCalendar(user, request));
    }
}
