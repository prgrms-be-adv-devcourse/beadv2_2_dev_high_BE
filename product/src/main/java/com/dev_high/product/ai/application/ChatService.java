package com.dev_high.product.ai.application;


import com.dev_high.product.ai.application.dto.ChatCommand;
import com.dev_high.product.ai.domain.ChatMessage;
import com.dev_high.product.ai.domain.ChatModel;
import com.dev_high.product.ai.domain.ChatResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class ChatService {

	private final ChatModel chatModel;

	// 단건 질의에 대한 동기 응답 전달
	public ChatResult testChat(ChatCommand command) {
		return chatModel.testChat(new ChatMessage(command.message()));
	}

	// SSE 스트리밍에 사용되는 토큰 Flux 반환
	public Flux<String> stream(String message) {
		return chatModel.stream(new ChatMessage(message));
	}
}
