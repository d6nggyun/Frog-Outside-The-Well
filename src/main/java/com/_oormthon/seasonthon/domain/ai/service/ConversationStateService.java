package com._oormthon.seasonthon.domain.ai.service;

import com._oormthon.seasonthon.domain.ai.entity.UserConversation;
import com._oormthon.seasonthon.domain.ai.enums.ConversationState;
import com._oormthon.seasonthon.domain.ai.repository.UserConversationRepository;
import com._oormthon.seasonthon.domain.ai.scripts.ChatbotScript;
import com._oormthon.seasonthon.domain.step.domain.TodoStep;
import com._oormthon.seasonthon.domain.step.repository.TodoStepRepository;
import com._oormthon.seasonthon.domain.todo.domain.Todo;
import com._oormthon.seasonthon.domain.todo.dto.res.TodoStepResponse;
import com._oormthon.seasonthon.domain.todo.enums.Day;
import com._oormthon.seasonthon.domain.todo.repository.TodoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
class ConversationStateService {

    private final UserConversationRepository conversationRepo;
    private final TodoStepRepository todoStepRepository;
    private final TodoRepository todoRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public UserConversation findOrCreateUserConversation(Long userId) {
        return conversationRepo.findByUserId(userId)
                .orElseGet(() -> {
                    try {
                        UserConversation uc = new UserConversation();
                        uc.setUserId(userId);
                        uc.setState(ConversationState.ASK_READY);
                        uc.setPlanSaved(false);
                        UserConversation saved = conversationRepo.saveAndFlush(uc);
                        return saved;
                    } catch (DataIntegrityViolationException e) {
                        log.warn("⚠️ 동시 생성 경합 발생 (userId={}) → 재조회 수행", userId);
                        return conversationRepo.findByUserId(userId)
                                .orElseThrow(() -> new IllegalStateException("UserConversation 생성 실패 후 조회 불가"));
                    }
                });
    }

    @Transactional
    public MessageResult processUserMessageTransactional(Long userId, String userMessage) {
        UserConversation convo = findOrCreateUserConversation(userId);

        String response = null;
        boolean streaming = false;
        String prompt = null;
        int stepIndex = 0;
        Long createdTodoId = null;
        try {
            switch (convo.getState()) {
                case ASK_READY -> {
                    response = ChatbotScript.readyResponse(userMessage);
                    convo.setState(ConversationState.ASK_NAME);
                }
                case ASK_NAME -> {
                    convo.setUserName(userMessage.trim());
                    response = ChatbotScript.askAge(convo.getUserName());
                    convo.setState(ConversationState.ASK_AGE);
                }
                case ASK_AGE_INTRO -> {
                    response = String.format("좋아, %s! 👋 이제 나이를 알려줄래? (예: 16)", convo.getUserName());
                    convo.setState(ConversationState.ASK_AGE);
                }
                case ASK_AGE -> {
                    try {
                        int age = Integer.parseInt(userMessage.trim());
                        convo.setUserAge(age);
                        response = ChatbotScript.ageResponse(age, convo.getUserName());
                        convo.setState(ConversationState.ASK_TASK);
                    } catch (NumberFormatException e) {
                        response = "숫자로 나이를 입력해줘! 예: 16";
                    }
                }
                case ASK_TASK_INTRO -> {
                    response = String.format(
                            "좋아, %s! 이제 어떤 목표를 세우고 싶어? 자세하게 말해줄수록 너를 돕기 쉬워지니까 잘 알려줘 :)🎯 예를 들어 ‘토익 800점 달성’처럼 말해줘!",
                            convo.getUserName());
                    convo.setState(ConversationState.ASK_TASK);
                }

                case ASK_TASK -> {
                    prompt = ChatbotScript.planDetail(convo.getUserAge(), userMessage.trim());
                    stepIndex = 1;
                    streaming = true;
                    convo.setState(ConversationState.ASK_START_DATE);
                }
                case ASK_START_DATE -> {
                    try {
                        LocalDate start = LocalDate.parse(userMessage.trim(),
                                DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                        if (start.isBefore(LocalDate.now())) {
                            response = String.format(
                                    "시작일은 오늘(%s) 이후여야 해! 다시 입력해줘 😊 (예: %s)",
                                    LocalDate.now(),
                                    LocalDate.now());
                            convo.setState(ConversationState.ASK_START_DATE);
                            break;
                        }

                        convo.setStartDate(start);
                        response = ChatbotScript.askEndDate(start);
                        convo.setState(ConversationState.ASK_END_DATE);
                    } catch (Exception e) {
                        response = "날짜는 'yyyy-MM-dd' 형식으로 입력해줘!";
                    }
                }
                case ASK_END_DATE -> {
                    try {
                        LocalDate end = LocalDate.parse(userMessage.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                        if (end.isBefore(convo.getStartDate())) {
                            response = "마감일은 시작일과 같거나 이후여야 해! 다시 입력해줘 😄 (예: 2025-12-31)";
                            convo.setState(ConversationState.ASK_END_DATE); // 다시 마감일 입력 대기
                            break;
                        }

                        convo.setEndDate(end);
                        response = ChatbotScript.askStudyDays(convo.getStartDate(), convo.getEndDate());
                        convo.setState(ConversationState.ASK_DAYS);
                    } catch (Exception e) {
                        response = "날짜는 'yyyy-MM-dd' 형식으로 입력해줘!";
                    }
                }
                case ASK_DAYS -> {
                    try {
                        // 입력한 요일들 파싱
                        List<Day> selectedDays = DayConverter.parseDays(userMessage.trim());

                        LocalDate start = convo.getStartDate();
                        LocalDate end = convo.getEndDate();

                        // 기간 내 존재하는 요일 목록 계산
                        List<Day> availableDays = DayConverter.daysBetween(start, end);

                        // 입력된 요일 중 실제 기간에 존재하는 요일이 있는지 확인
                        boolean hasValid = selectedDays.stream().anyMatch(availableDays::contains);

                        if (!hasValid) {
                            // 사용자가 입력한 요일이 기간에 하나도 없음 → 안내 메시지
                            String availableLabel = DayConverter.formatDays(availableDays);

                            response = String.format(
                                    "😮 이 기간(%s ~ %s)에는 네가 입력한 요일이 없어!\n" +
                                            "가능한 요일은 👉 %s \n" +
                                            "다시 입력해줘! (예: 월,수,금)",
                                    start, end, availableLabel);

                            convo.setState(ConversationState.ASK_DAYS);
                            break;
                        }

                        String formattedDays = DayConverter.formatDays(selectedDays);
                        convo.setStudyDays(formattedDays);

                        response = "좋아! 한 번 공부할 때 몇 분 정도 할지 숫자만 입력해줘. (예: 30)";
                        convo.setState(ConversationState.ASK_TIME_PER_DAY);
                    } catch (Exception e) {
                        response = "(예: 월,수,금) 형식으로 작성해줘!";
                    }

                }
                case ASK_TIME_PER_DAY -> {
                    try {
                        int minutes = Integer.parseInt(userMessage.trim());
                        convo.setDailyMinutes(minutes);
                        prompt = ChatbotScript.planPrompt(convo);
                        stepIndex = 2;
                        streaming = true;
                        convo.setState(ConversationState.CONFIRM_PLAN);
                    } catch (NumberFormatException e) {
                        response = "공부 시간은 숫자로 입력해줘! 예: 90";
                    }
                }
                case CONFIRM_PLAN -> {
                    if (userMessage.contains("좋아") || userMessage.contains("응") || userMessage.contains("저장")) {
                        try {
                            if (convo.getPendingPlanJson() != null && !convo.isPlanSaved()) {
                                TodoStepResponse parsed = objectMapper.readValue(convo.getPendingPlanJson(),
                                        TodoStepResponse.class);

                                Todo todo = Todo.builder()
                                        .userId(userId)
                                        .title(convo.getTitle())
                                        .content(convo.getContent())
                                        .startDate(convo.getStartDate())
                                        .endDate(convo.getEndDate())
                                        .progress(0)
                                        .expectedDays(DayConverter.parseDays(convo.getStudyDays()))
                                        .isCompleted(false)
                                        .build();

                                todoRepository.save(todo);
                                createdTodoId = todo.getId();

                                List<TodoStep> todoSteps = parsed.steps().stream()
                                        .map(step -> TodoStep.builder()
                                                .todoId(todo.getId())
                                                .userId(userId)
                                                .stepDate(step.stepDate())
                                                .day(step.day())
                                                .tips(step.tips())
                                                .description(step.description())
                                                .build())
                                        .toList();
                                todoStepRepository.saveAll(todoSteps);

                                convo.setPlanSaved(true);
                                convo.setPendingPlanJson(null); // ✅ 임시 JSON 제거
                                conversationRepo.save(convo);
                            }
                        } catch (Exception e) {
                            log.error("❌ CONFIRM_PLAN 단계 저장 중 오류", e);
                        }

                        response = "좋아! 🎉 이 계획으로 진행할게. 화이팅 💪\n (TodoId=" + createdTodoId + ")";
                        convo.setState(ConversationState.FINISHED);
                    } else if (userMessage.contains("아니") || userMessage.contains("수정")) {
                        convo.setPendingPlanJson(null);
                        convo.setState(ConversationState.CHECK_MODIFY); // ✅ 수정 선택 단계로 이동
                        response = """
                                괜찮아 😊 어떤 부분을 수정할까?
                                - 목표
                                - 시작일
                                - 마감일
                                - 공부시간(요일)
                                중에서 말해줘!
                                """;
                    } else {
                        response = "이 계획으로 진행할까? (좋아 / 응 / 아니 / 수정 / 저장)으로 답해줘";
                    }
                }
                case CHECK_MODIFY -> {
                    if (userMessage.contains("목표")) {
                        convo.setState(ConversationState.ASK_TASK);
                        response = "좋아! 🎯 새 목표를 알려줘.";
                    } else if (userMessage.contains("시작") || userMessage.contains("시작일")) {
                        convo.setState(ConversationState.ASK_START_DATE);
                        response = "언제부터 시작할까? (예: 2025-11-10)";
                    } else if (userMessage.contains("마감") || userMessage.contains("종료") || userMessage.contains("끝")) {
                        convo.setState(ConversationState.ASK_END_DATE);
                        response = "언제까지 목표를 이루고 싶어? (예: 2025-12-31)";
                    } else if (userMessage.contains("요일")) {
                        convo.setState(ConversationState.ASK_DAYS);
                        response = "공부할 요일을 다시 알려줄래? (예: 월,수,금)";
                    } else if (userMessage.contains("시간")) {
                        convo.setState(ConversationState.ASK_TIME_PER_DAY);
                        response = "공부 시간을 다시 숫자로 입력해줘! 예: 90";
                    } else {
                        convo.setState(ConversationState.CHECK_MODIFY); // 🔁 반복 대기
                        response = """
                                괜찮아 😊 어떤 부분을 수정할까?
                                - 목표
                                - 시작일
                                - 마감일
                                - 공부요일
                                - 하루 학습 시간
                                중에서 말해줘!
                                """;
                    }
                }

                case FINISHED -> {
                    if (userMessage.contains("새로운 계획")) {
                        convo.setState(ConversationState.ASK_TASK);
                        convo.setTitle(null);
                        convo.setContent(null);
                        convo.setPendingPlanJson(null);
                        convo.setStartDate(null);
                        convo.setEndDate(null);
                        convo.setStudyDays(null);
                        convo.setDailyMinutes(0);
                        convo.setPlanSaved(false);
                        response = "좋아! 🐸 새로운 공부 계획을 세워보자! 이번에 이루고 싶은 목표가 뭐야? 예를 들어 ‘토익 800점 달성’ 같은 거!";
                    } else {
                        response = "이미 계획이 완성됐어 🎯 '새로운 계획'이라고 말해줘!";
                    }
                }
                default -> response = "무슨 말인지 모르겠어 😅 다시 말해줄래?";
            }

            conversationRepo.save(convo);
        } catch (Exception e) {
            log.error("💥 메시지 처리 오류", e);
            response = "오류가 발생했어 😢 다시 시도해줘.";
        }

        return new MessageResult(response, prompt, streaming, stepIndex);
    }

    public static record MessageResult(String response, String prompt, boolean isStreaming, int stepIndex) {
    }
}
