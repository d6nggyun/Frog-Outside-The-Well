package com._oormthon.seasonthon.domain.ai.enums;

public enum ConversationState {

    // INTRO, // 챗봇 자기소개
    ASK_READY, // "준비됐어?" 질문
    ASK_NAME, // 이름 묻기
    ASK_AGE_INTRO,
    ASK_AGE, // 나이 묻기
    ASK_TASK_INTRO,
    ASK_TASK, // 해야 할 일(목표) 묻기
    ASK_START_DATE, // 시작일 묻기
    ASK_END_DATE, // 종료일 묻기
    ASK_DAYS, // 요일 묻기
    ASK_TIME_PER_DAY, // 하루 공부 시간 묻기
    SHOW_PLAN, // 계획 생성 및 확인
    CONFIRM_PLAN, // 계획 확정 여부 묻기
    FINISHED // 완료
}
