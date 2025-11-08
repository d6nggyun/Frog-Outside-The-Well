package com._oormthon.seasonthon.domain.dailyLog.domain;

import com._oormthon.seasonthon.domain.member.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

import com._oormthon.seasonthon.domain.dailyLog.dto.request.DailyLogAfterRequest;
import com._oormthon.seasonthon.domain.dailyLog.enums.CompletionLevel;
import com._oormthon.seasonthon.domain.dailyLog.enums.Mood;

@Entity
@Table(name = "daily_log_after")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyLogAfter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 기분 (즐거움, 설렘, 평온, 그저그래, 짜릿, 답답, 우울, 허무, 화, 실망)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Mood mood;

    @Column(name = "user_id", nullable = true)
    private Long userId;

    // 집중도: 1~5
    @Min(1)
    @Max(5)
    @Column(nullable = true)
    private Integer focusLevel;

    // 완성도: 0, 25, 50, 75, 100
    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 10)
    private CompletionLevel completionLevel;

    // 메모
    @Column(columnDefinition = "TEXT", nullable = true)
    private String memo;

    // 사진 (파일 경로나 URL)
    @Column(nullable = true)
    private String photoUrl;

    // 작성날
    @Column(nullable = true)
    private LocalDate createdAt;

    public static DailyLogAfter createDailyLogAfter(User user, DailyLogAfterRequest dailyLogAfterRequest) {
        return DailyLogAfter.builder()
                .userId(user.getUserId())
                .mood(dailyLogAfterRequest.mood())
                .focusLevel(dailyLogAfterRequest.focusLevel())
                .completionLevel(dailyLogAfterRequest.completionLevel())
                .memo(dailyLogAfterRequest.memo())
                .photoUrl(dailyLogAfterRequest.photoUrl())
                .build();
    }

}
