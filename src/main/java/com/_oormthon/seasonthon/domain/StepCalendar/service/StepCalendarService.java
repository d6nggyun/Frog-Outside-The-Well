package com._oormthon.seasonthon.domain.StepCalendar.service;

import com._oormthon.seasonthon.domain.StepCalendar.domain.StepCalendar;
import com._oormthon.seasonthon.domain.StepCalendar.dto.req.StepCalendarRequest;
import com._oormthon.seasonthon.domain.StepCalendar.dto.res.StepCalendarResponse;
import com._oormthon.seasonthon.domain.StepCalendar.repository.StepCalendarRepository;
import com._oormthon.seasonthon.domain.member.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StepCalendarService {

    private final StepCalendarRepository stepCalendarRepository;

    @Transactional(readOnly = true)
    public List<StepCalendarResponse> findTodoCalendar(User user, StepCalendarRequest request) {
        YearMonth yearMonth = YearMonth.of(request.year(), request.month());
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<StepCalendar> stepCalendars = stepCalendarRepository
                .findAllByUserIdAndCalendarDateBetween(user.getUserId(), startDate, endDate);

        return stepCalendars.stream().map(StepCalendarResponse::from).toList();
    }
}
