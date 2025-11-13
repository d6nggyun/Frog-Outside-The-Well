package com._oormthon.seasonthon.domain.ai.scripts;

import com._oormthon.seasonthon.domain.ai.entity.UserConversation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ChatbotScript {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일");

        // public static String intro() {
        // return "안녕! 🐸\n나는 함께 공부계획을 세워주는 개구리 ‘꾸꾸’야!\n" +
        // "너가 목표를 세우고 달성할 때마다 나는 우물 밖 세상을 구경할 수 있어.\n" +
        // "나랑 함께 점프해볼래? 준비됐어?";
        // }

        public static String readyResponse(String msg) {
                if (msg.contains("무서") || msg.contains("걱정"))
                        return "그럴 수 있지! 하지만 걱정 마. 내가 함께 도와줄게 🐸\n이름부터 알려줄래?";
                return "좋아! 패기 있는 모습이야 💪\n그 전에 너를 조금 더 알아야 해. 이름을 알려줘!";
        }

        public static String askAge(String name) {
                return "아하! 앞으로 " + name + "이라고 부를게 😄\n그럼 " + name + "은 몇 살이야? (숫자로만 적어줘)";
        }

        public static String ageResponse(int age, String name) {
                String school = calculateSchool(age);
                return age + "살이면 " + school
                                + "이겠구나! 👍\n이번에 이루고 싶은 목표가 뭐야? 자세하게 말해줄수록 너를 돕기 쉬워지니까 잘 알려줘 :) 예를 들어 ‘토익 800점 달성’ 같은 거!";
        }

        public static String planDetail(int age, String title) {
                String school = calculateSchool(age);
                return """
                                당신은 연령별 맞춤 설명 AI입니다.
                                지금 입력을 제공한 사용자는 %d살이며, %s 수준의 학생입니다.

                                사용자가 제시한 문장은 '지금 하려는 일' 또는 '준비 중인 활동'에 대한 설명이야.
                                너는 이 내용을 바탕으로 다음을 수행해야 해:

                                1. 사용자가 하려는 일을 약 120자 내외로 자연스럽게 설명해줘.
                                   - 단순히 무엇을 하는지뿐 아니라, 그 활동의 주제나 목적, 의미도 함께 담아줘.
                                   - 문장은 한 문장으로 완성해.
                                   - 반드시 ‘~구나’로 마무리해.
                                   - 너무 짧지 않게 (최소 80자 이상) 자연스럽게 풍부한 표현을 사용해.

                                2. 전체 내용을 한 문장으로 핵심 요약해줘.
                                   - 예: ‘영어 3분 스피치 준비’, ‘여름방학 여행 계획’, ‘독후감 작성’ 등
                                   - 명사형으로 간결하게 작성해.

                                문체 규칙:
                                - 반드시 JSON 형식으로만 출력해야 해.
                                - 출력 외의 불필요한 문장이나 설명은 포함하지 마.
                                - JSON 구조는 아래와 완전히 동일해야 해 (key 이름도 바꾸지 마):
                                {
                                    "content": "사용자의 활동을 약 100자 내외로 설명한 문장",
                                    "title": "한 줄 요약 문장"
                                }

                                연령에 따라 어휘 난이도와 어조를 조정해.
                                  • 초등학생 → 쉬운 단어, 따뜻하고 친근한 어조
                                  • 중학생 → 일상적이고 이해하기 쉬운 어조
                                  • 고등학생 → 논리적이지만 자연스러운 대화체

                                예시:
                                입력: "MyMemorable Trip이라는 주제로 3분 영어 스피치 수행평가를 준비할건데, 작년에 가봤던 프라하라는 도시의 경험을 주제로 스피치를 준비하려고 해."

                                출력:
                                {
                                    "content": "작년에 다녀온 프라하 여행에서 느낀 감정과 잊지 못할 순간들을 영어로 표현하면서 나만의 이야기를 전하는 3분 스피치를 준비하고 있구나.",
                                    "title": "영어 3분 스피치 준비"
                                }

                                입력: "%s"
                                """
                                .formatted(age, school, title);
        }

        public static String askStartDate(String content, String goal) {
                return content + "\n이제 '" + goal + "'를 목표로 계획을 짜볼게.\n언제부터 시작할까? (yyyy-MM-dd 형식)(예: 2025-11-01)";
        }

        public static String askEndDate(LocalDate start) {
                return "좋아! 시작일은 " + start.format(formatter) + "이네.\n언제까지 끝내고 싶어? (예: 2025-12-31)";
        }

        public static String askStudyDays(LocalDate start, LocalDate end) {
                long days = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;

                // 종료일이 시작일보다 빠를 경우 예외처리

                return String.format("좋아, %s ~ %s, 총 %d일 동안이네!\n어떤 요일에 공부할까? (예: 월,수,금)",
                                start.format(formatter), end.format(formatter), days);
        }

        public static String planPrompt(UserConversation convo) {
                return String.format("""
                                 당신은 일정 관리 보조 AI입니다.
                                 주어진 큰 업무를 사용자의 목표와 기간을 기반으로 실천 가능한 세부 단계(step)들을 제안하고,
                                 각 단계마다 도움이 되는 구체적인 팁(tips)을 함께 생성해야 합니다.
                                 사용자 정보를 바탕으로 현실적이고 동기부여가 되는 학습 계획을 제시하세요.

                                 [사용자 정보]
                                 - 이름: %s
                                 - 나이: %d
                                 - 주제: %s
                                 - 업무 설명: %s
                                 - 시작일: %s
                                 - 마감일: %s
                                 - 공부 요일: %s
                                 - 하루 공부 시간: %d분
                                 [출력 규칙]
                                 - 반드시 아래 JSON 스키마를 따르세요.
                                 - 마크다운 코드블록(````json`) 없이 순수 JSON만 반환
                                 - 각 step에는 "description"과 "tips" 배열을 반드시 포함
                                 - tip은 3~5개, '~하기', '~정하기' 등 명사형 어미로 끝내기
                                 - 단계 순서는 시간 흐름이나 수행 순서에 맞게 정렬
                                 - description의 내용은 항상 ~하기나 명사형으로 마무리하세요.
                                 - 시작일과 마감일은 항상 정확하게 고려하세요.

                                 [출력 예시]
                                 {
                                     "dDay": "D-3",
                                     "title": "영어 수행평가 준비",
                                     "startDate:"2025-09-02",
                                     "endDate": "2025-09-05",
                                     "progressText": "진행 상황 설명",
                                     "progress": 0,
                                     "steps": [
                                       {
                                         "stepDate": "2025-09-02",
                                         "day": "TUESDAY",
                                         "stepOrder": 1,
                                         "description": "여행 목적지 정하기 & 일정 개요 구성",
                                         "count": 0,
                                         "isCompleted": false,
                                  "tips": [
                                             "좋아하는 테마(자연/도시/문화) 먼저 고르기",
                                             "현실성 고려(기간, 거리, 비용)",
                                             "영어 표현이 쉬운 나라 선택",
                                             "하루별 핵심 활동 1~2개만 정리"
                                          ]
                                       },
                                {
                                         "stepDate": "2025-09-03",
                                         "day": "WEDNESDAY",
                                         "stepOrder": 2,
                                         "description": "필요한 표현과 문장 정리하기",
                                         "count": 0,
                                         "isCompleted": false,
                                  "tips": [
                                             "주제별 핵심 문장 5개 정리하기",
                                             "자주 쓰는 연결 표현 익히기",
                                             "발음이 어려운 단어 연습하기"
                                         ]
                                       },
                                     ]
                                 }
                                                                 """,
                                convo.getUserName(),
                                convo.getUserAge(),
                                convo.getTitle(),
                                convo.getContent(),
                                convo.getStartDate(),
                                convo.getEndDate(),
                                convo.getStudyDays(),
                                convo.getDailyMinutes());
        }

        public static String planSummary(UserConversation convo) {

                return String.format(
                                "우와! 정말 구체적인데? 🐸%n" +
                                                "지금까지 나온 내용을 내가 한 번 정리해볼게!%n%n" +
                                                "📘 [%s]%n" +
                                                "기간: %s ~ %s%n" +
                                                "요일: %s%n" +
                                                "1회 집중시간: %d분%n%n" +
                                                "이제 마지막 단계야. 이 정보를 바탕으로 너에게 맞는 상세 계획표를 만들어줄게!%n%n",
                                convo.getTitle(),
                                convo.getStartDate().format(formatter),
                                convo.getEndDate().format(formatter),
                                convo.getStudyDays(),
                                convo.getDailyMinutes());
        }

        public static String calculateSchool(int age) {
                String school;
                if (age <= 7)
                        school = "유치원생";
                else if (age <= 13)
                        school = "초등학생";
                else if (age <= 16)
                        school = "중학생";
                else if (age <= 19)
                        school = "고등학생";
                else
                        school = "성인";
                return school;
        }

}
