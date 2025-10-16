package com._oormthon.seasonthon.diary;

import com._oormthon.seasonthon.domain.diary.dto.res.DiaryResponse;
import com._oormthon.seasonthon.domain.diary.service.DiaryService;
import com._oormthon.seasonthon.domain.member.entity.DailyLogAfter;
import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.member.enums.CompletionLevel;
import com._oormthon.seasonthon.domain.member.enums.Mood;
import com._oormthon.seasonthon.domain.member.repository.DailyLogAfterRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class DiaryServiceTest {

    @Autowired
    DailyLogAfterRepository dailyLogAfterRepository;

    @Autowired
    DiaryService diaryService;

    @DisplayName("특정 YearMonth 내의 일기 Mood 데이터들을 올바르게 조회한다")
    @Test
    void findDiaries() {

        User user = User.builder().userId(1L).email("email").build();

        DailyLogAfter log1 = DailyLogAfter.builder()
                .userId(1L)
                .mood(Mood.HAPPY)
                .focusLevel(1)
                .completionLevel(CompletionLevel.ZERO)
                .build();

        DailyLogAfter log2 = DailyLogAfter.builder()
                .userId(1L)
                .mood(Mood.EXCITED)
                .focusLevel(1)
                .completionLevel(CompletionLevel.ZERO)
                .build();

        dailyLogAfterRepository.saveAll(List.of(log1, log2));
        dailyLogAfterRepository.flush();

        YearMonth yearMonth = YearMonth.of(LocalDate.now().getYear(), LocalDate.now().getMonth());

        List<DiaryResponse> result = diaryService.findDiaries(user, yearMonth);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).mood()).isEqualTo(Mood.HAPPY);
        assertThat(result.get(1).mood()).isEqualTo(Mood.EXCITED);

        assertThat(result.get(0).date()).isEqualTo(LocalDate.now());
        assertThat(result.get(1).date()).isEqualTo(LocalDate.now());
    }
}
