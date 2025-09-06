package com._oormthon.seasonthon.domain.member.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

import com._oormthon.seasonthon.domain.member.dto.request.DailyLogAfterRequest;
import com._oormthon.seasonthon.domain.member.enums.CompletionLevel;
import com._oormthon.seasonthon.domain.member.enums.Mood;

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

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 집중도: 1~5
    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private int focusLevel;

    // 완성도: 0, 25, 50, 75, 100
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CompletionLevel completionLevel;

    // 메모
    @Column(columnDefinition = "TEXT")
    private String memo;

    // 사진 (파일 경로나 URL)
    private String photoUrl;

    // 작성날 (자동 저장)
    @CreationTimestamp
    @Column(updatable = false, nullable = false)
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
