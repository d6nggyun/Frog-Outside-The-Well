package com._oormthon.seasonthon.domain.ai.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com._oormthon.seasonthon.domain.ai.entity.UserConversation;
import com._oormthon.seasonthon.domain.ai.enums.ConversationState;
import com._oormthon.seasonthon.domain.ai.repository.UserConversationRepository;
import com._oormthon.seasonthon.domain.ai.scripts.ChatbotScript;
import com._oormthon.seasonthon.domain.step.domain.TodoStep;
import com._oormthon.seasonthon.domain.step.repository.TodoStepRepository;
import com._oormthon.seasonthon.domain.todo.domain.Todo;
import com._oormthon.seasonthon.domain.todo.dto.res.TodoStepResponse;
import com._oormthon.seasonthon.domain.todo.repository.TodoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
                        log.info("🆕 새 대화 생성 (userId={})", userId);
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
                case ASK_TASK -> {
                    convo.setTitle(userMessage.trim());
                    prompt = ChatbotScript.planDetail(convo.getUserAge(), userMessage.trim());
                    stepIndex = 1;
                    streaming = true;
                    convo.setState(ConversationState.ASK_START_DATE);
                }
                case ASK_START_DATE -> {
                    try {
                        LocalDate start = LocalDate.parse(userMessage.trim(),
                                DateTimeFormatter.ofPattern("yyyy-MM-dd"));
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
                        convo.setEndDate(end);
                        response = ChatbotScript.askStudyDays(convo.getStartDate(), convo.getEndDate());
                        convo.setState(ConversationState.ASK_DAYS);
                    } catch (Exception e) {
                        response = "날짜는 'yyyy-MM-dd' 형식으로 입력해줘!";
                    }
                }
                case ASK_DAYS -> {
                    try {
                        // 반환값 활용하도록 변경 권장
                        DayConverter.parseDays(userMessage.trim());
                        convo.setStudyDays(userMessage.trim());
                        response = "좋아! 하루 공부 시간을 (분 단위로) 알려줘.";
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

                                List<TodoStep> todoSteps = parsed.steps().stream()
                                        .map(step -> TodoStep.builder()
                                                .todoId(todo.getId())
                                                .userId(userId)
                                                .stepDate(step.stepDate())
                                                .description(step.description())
                                                .isCompleted(step.isCompleted())
                                                .build())
                                        .toList();
                                todoStepRepository.saveAll(todoSteps);

                                convo.setPlanSaved(true);
                                convo.setPendingPlanJson(null); // ✅ 임시 JSON 제거
                                conversationRepo.save(convo);

                                log.info("💾 CONFIRM_PLAN 단계에서 Todo 및 Steps 최종 저장 완료 (userId={})", userId);
                            }
                        } catch (Exception e) {
                            log.error("❌ CONFIRM_PLAN 단계 저장 중 오류", e);
                        }

                        response = "좋아! 🎉 이 계획으로 진행할게. 화이팅 💪";
                        convo.setState(ConversationState.FINISHED);
                    } else if (userMessage.contains("아니") || userMessage.contains("수정")) {
                        convo.setPendingPlanJson(null); // ❌ 기존 계획 삭제
                        response = "괜찮아 😊 어떤 점을 수정할까? 목표부터 다시 정해보자";
                        convo.setState(ConversationState.ASK_TASK);
                    } else {
                        response = "이 계획으로 진행할까? (좋아 / 응 / 아니 / 수정 / 저장)으로 답해줘줘";
                    }
                }

                case FINISHED -> {
                    if (userMessage.contains("새로운 계획")) {
                        convo.setState(ConversationState.ASK_READY);
                        response = "좋아! 🐸 새로운 공부 계획을 세워보자!";
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