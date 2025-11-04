package com._oormthon.seasonthon.domain.ai.scripts;

import com._oormthon.seasonthon.domain.ai.entity.UserConversation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ChatbotScript {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M월 d일");

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
                return age + "살이면 " + school + "이겠구나! 👍\n이번에 이루고 싶은 목표가 뭐야? 예를 들어 ‘토익 800점 달성’ 같은 거!";
        }

        public static String planDetail(int age, String title) {
                return """
                                당신은 문장 확장 전문 AI입니다.
                                %d살의 사용자가 제시한 짧은 문장(예: 할 일, 주제, 키워드)을 기반으로
                                그 내용에 대한 자연스럽고 구체적인 설명 문장을 만들어야 합니다.

                                출력 문장은 반드시 '~이야', '~있어', '~돼' 등의 자연스러운 종결어미로 끝나야 합니다.
                                불필요한 문어체 표현은 피하고, 일상 대화처럼 매끄럽게 표현하세요.
                                사용자의 나이를 참고해 어투를 자연스럽게 조정하세요.
                                출력은 한 문장으로만 작성합니다.

                                예시:
                                입력: '박태웅의 AI특강으로 독후감'
                                출력: '박태웅 의장의 AI 특강은 AI 기술의 발전이 우리 삶과 경제 구조에 미치는 영향, 그리고 이에 따른 사회적 변화와 윤리적 고려사항을 다루고 있어.'

                                입력: '여름 휴가 계획 세우기'
                                출력: '여름 휴가 계획은 더운 날씨를 피해서 가족과 함께 쉴 수 있는 여행지를 정하고 예산을 준비하는 거야.'

                                입력: '%s'
                                """.formatted(age, title);
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
                                주어진 큰 업무를 실천 가능한 작은 Todo 항목들로 나누세요.
                                사용자 정보를 바탕으로 현실적이고 동기부여가 되는 학습 계획을 제시하세요.

                                [사용자 정보]
                                - 이름: %s
                                - 나이: %d
                                - 주제: %s
                                - 업무 설명: %s
                                - 기간: %s ~ %s
                                - 공부 요일: %s
                                - 하루 공부 시간: %d분
                                반드시 아래 JSON 스키마를 따르세요.
                                마크다운 코드블록(````json`) 없이 순수 JSON만 반환하세요.

                                description의 내용은 항상 ~하기나 명사형으로 마무리하세요.

                                시작일과 마감일은 항상 정확하게 고려하세요.
                                {
                                    "dDay": "D-3",
                                    "title": "큰 업무 제목",
                                    "endDate": "2025-09-05",
                                    "progressText": "진행 상황 설명",
                                    "progress": 0,
                                    "steps": [
                                      {
                                        "stepDate": "2025-09-02",
                                        "stepOrder": 1,
                                        "description": "세부 작업 설명",
                                        "count": 0,
                                        "isCompleted": false
                                      }
                                    ]
                                }
                                                                """,
                                convo.getUserName(),
                                convo.getUserAge(),
                                convo.getTitle(),
                                convo.getContent(),
                                convo.getStartDate().format(formatter),
                                convo.getEndDate().format(formatter),
                                convo.getStudyDays(),
                                convo.getDailyMinutes());
        }

        public static String planSummary(UserConversation convo) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M월 d일");

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

}
