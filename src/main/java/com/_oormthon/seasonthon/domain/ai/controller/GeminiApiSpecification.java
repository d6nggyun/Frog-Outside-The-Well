package com._oormthon.seasonthon.domain.ai.controller;

import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.todo.dto.res.TodoStepResponse;
import com._oormthon.seasonthon.global.exception.ErrorResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "AI ToDo", description = "AI 기반 ToDo 분해 API")
public interface GeminiApiSpecification {

        @Operation(summary = "AI ToDo 분해", description = "회원이 입력한 큰 업무를 AI가 작은 Step 단위의 Todo로 분해합니다. <br><br>" +
                        "각 Step은 날짜(stepDate), 순서(stepOrder), 내용(description), 완료 여부(isCompleted) 정보를 포함합니다.<br><br>"
                        + "todoSteps는 빈 리스트로 넣으면 됩니다.", responses = {
                                        @ApiResponse(responseCode = "201", description = "AI ToDo 분해 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TodoStepResponse.class))),
                                        @ApiResponse(responseCode = "500", description = "서버 오류 발생", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseEntity.class)))
                        })
        ResponseEntity<TodoStepResponse> breakdownTodo(@AuthenticationPrincipal User user, @PathVariable Long todoId);

        // Flux<String> streamChat(@RequestParam Long userId, @RequestParam String
        // message);

}
