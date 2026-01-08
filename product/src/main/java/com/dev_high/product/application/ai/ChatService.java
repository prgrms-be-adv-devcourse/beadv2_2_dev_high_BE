package com.dev_high.product.application.ai;


import com.dev_high.product.application.ai.dto.ChatCommand;
import com.dev_high.product.domain.ai.ChatMessage;
import com.dev_high.product.domain.ai.ChatModel;
import com.dev_high.product.domain.ai.ChatResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class ChatService {

	private final ChatModel chatModel;

	// 단건 질의에 대한 동기 응답 전달
	public ChatResult chat(ChatCommand command) {
		return chatModel.chat(new ChatMessage(command.message()));
	}

	// SSE 스트리밍에 사용되는 토큰 Flux 반환
	public Flux<String> stream(String message) {
		return chatModel.stream(new ChatMessage(message));
	}
}
