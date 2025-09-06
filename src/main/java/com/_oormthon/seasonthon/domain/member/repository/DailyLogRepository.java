// package com._oormthon.seasonthon.domain.member.repository;

// import com._oormthon.seasonthon.domain.member.entity.DailyLog;
// import org.springframework.data.jpa.repository.JpaRepository;

// import java.time.LocalDate;
// import java.util.Optional;
// import java.util.List;

// public interface DailyLogRepository extends JpaRepository<DailyLog, Long> {
//     Optional<DailyLog> findByUser_UserIdAndLogDate(Long userId, LocalDate logDate);

//     List<DailyLog> findByUser_UserIdAndLogDateBetween(Long userId, LocalDate start, LocalDate end);

//     boolean existsByUser_UserIdAndLogDate(Long userId, LocalDate logDate);
// }