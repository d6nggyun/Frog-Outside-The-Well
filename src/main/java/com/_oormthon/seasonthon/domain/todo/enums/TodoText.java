package com._oormthon.seasonthon.domain.todo.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TodoText {

    // Progress
    PROGRESS_100("개구리가 드디어 우물을 탈출했어요!"),
    PROGRESS_80("이제 개구리가 바깥 공기를 느끼고 있네요!"),
    PROGRESS_50("오르는 데 고비가 있지만 잘 견뎌내고 있어요!"),
    PROGRESS_20("개구리가 우물 벽을 오르기 시작했네요!"),
    PROGRESS_0("우물에 햇빛이 들기 시작했어요!"),

    // Warm Text
    WARM_TEXT_1("매일의 발자국이 모여 큰 걸음이 될거야."),
    WARM_TEXT_2("일을 시작하기 전에 심호흡 한 번 크게 해볼까?"),
    WARM_TEXT_3("마법의 주문을 외워보자, 나는 할 수 있다."),
    WARM_TEXT_4("나... 우물 밖 세상을 구경하고 싶어.")
    ;

    private final String text;
}
