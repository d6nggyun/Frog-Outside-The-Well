// package com._oormthon.seasonthon.domain.member.service.impl;

// import com._oormthon.seasonthon.domain.member.dto.request.DailyLogCreateRequest;
// import com._oormthon.seasonthon.domain.member.dto.request.DailyLogUpdateRequest;
// import com._oormthon.seasonthon.domain.member.dto.response.DailyLogResponse;
// import com._oormthon.seasonthon.domain.member.entity.DailyLog;
// import com._oormthon.seasonthon.domain.member.entity.User;
// import com._oormthon.seasonthon.domain.member.repository.DailyLogRepository;
// import com._oormthon.seasonthon.domain.member.repository.UserRepository;
// import com._oormthon.seasonthon.domain.member.service.DailyLogService;
// import com._oormthon.seasonthon.global.exception.DuplicateResourceException;
// import com._oormthon.seasonthon.global.exception.ResourceNotFoundException;

// import lombok.RequiredArgsConstructor;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.LocalDate;
// import java.util.List;
// import java.util.stream.Collectors;

// @Service
// @RequiredArgsConstructor
// public class DailyLogServiceImpl implements DailyLogService {

//     private final DailyLogRepository dailyLogRepository;
//     private final UserRepository userRepository;

//     @Override
//     @Transactional
//     public DailyLogResponse createDailyLog(Long userId, DailyLogCreateRequest req) {

//         if (dailyLogRepository.existsByUser_UserIdAndLogDate(userId, req.getLogDate())) {
//             throw new DuplicateResourceException("DailyLog already exists for date: " + req.getLogDate());
//         }

//         DailyLog log = DailyLog.builder()
//                 .userId(userId)
//                 .logDate(req.getLogDate())
//                 .emotion(req.getEmotion())
//                 .focusLevel(req.getFocusLevel())
//                 .completionLevel(req.getCompletionLevel())
//                 .memo(req.getMemo())
//                 .photoUrl(req.getPhotoUrl())
//                 .build();

//         DailyLog saved = dailyLogRepository.save(log);
//         return DailyLogResponse.fromEntity(saved);
//     }

//     @Override
//     @Transactional(readOnly = true)
//     public DailyLogResponse getDailyLog(Long userId, LocalDate date) {
//         DailyLog log = dailyLogRepository.findByUser_UserIdAndLogDate(userId, date)
//                 .orElseThrow(() -> new ResourceNotFoundException("DailyLog not found for date: " + date));
//         return DailyLogResponse.fromEntity(log);
//     }

//     @Override
//     @Transactional(readOnly = true)
//     public List<DailyLogResponse> listDailyLogs(Long userId, LocalDate start, LocalDate end) {
//         List<DailyLog> list = dailyLogRepository.findByUser_UserIdAndLogDateBetween(userId, start, end);
//         return list.stream().map(DailyLogResponse::fromEntity).collect(Collectors.toList());
//     }

//     @Override
//     @Transactional
//     public DailyLogResponse updateDailyLog(Long userId, LocalDate date, DailyLogUpdateRequest req) {
//         DailyLog log = dailyLogRepository.findByUser_UserIdAndLogDate(userId, date)
//                 .orElseThrow(() -> new ResourceNotFoundException("DailyLog not found for date: " + date));

//         if (req.getEmotion() != null)
//             log.setEmotion(req.getEmotion());
//         if (req.getFocusLevel() != null)
//             log.setFocusLevel(req.getFocusLevel());
//         if (req.getCompletionLevel() != null)
//             log.setCompletionLevel(req.getCompletionLevel());
//         if (req.getMemo() != null)
//             log.setMemo(req.getMemo());
//         if (req.getPhotoUrl() != null)
//             log.setPhotoUrl(req.getPhotoUrl());

//         DailyLog saved = dailyLogRepository.save(log);
//         return DailyLogResponse.fromEntity(saved);
//     }

//     @Override
//     @Transactional
//     public void deleteDailyLog(Long userId, LocalDate date) {
//         DailyLog log = dailyLogRepository.findByUser_UserIdAndLogDate(userId, date)
//                 .orElseThrow(() -> new ResourceNotFoundException("DailyLog not found for date: " + date));
//         dailyLogRepository.delete(log);
//     }
// }
