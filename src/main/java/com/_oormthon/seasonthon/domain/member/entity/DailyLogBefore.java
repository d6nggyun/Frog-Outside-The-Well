package com._oormthon.seasonthon.domain.member.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import com._oormthon.seasonthon.domain.member.enums.PlaceType;

import java.time.LocalDate;

@Entity
@Table(name = "daily_log_before")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyLogBefore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 감정: 1~5
    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private int emotion;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 에너지: 1~5
    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private int energy;

    // 장소 (집, 직장, 카페, 도서관, 강의실, 기타)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlaceType place;

    // 작성날 (자동 저장)
    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDate createdAt;

}
