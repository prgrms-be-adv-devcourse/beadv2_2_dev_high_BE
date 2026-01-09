package com.dev_high.product.presentation.ai;


import com.dev_high.product.application.ai.ChatService;
import com.dev_high.product.application.ai.dto.ChatInfo;
import com.dev_high.product.domain.ai.ChatResult;
import com.dev_high.product.presentation.ai.dto.ChatRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("${api.v1:/api/v1}/chat")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chat", description = "LLM 챗봇 API")
public class ChatController {
    // 환경에서 주입된 API 키를 확인용으로 로깅에 사용
    private final ChatService chatService;

    @PostMapping
    @Operation(summary = "단건 챗봇 응답")
    public ChatInfo testChat(@Valid @RequestBody ChatRequest request) {
        // 동기 방식으로 단일 질문에 대한 답변을 생성

        ChatResult result = chatService.testChat(request.toCommand());
        return new ChatInfo(result.content(), result.metadata());
    }

}