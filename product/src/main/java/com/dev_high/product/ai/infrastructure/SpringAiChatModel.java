package com.dev_high.product.infraStructure.ai;

import com.dev_high.product.domain.ai.ChatMessage;
import com.dev_high.product.domain.ai.ChatModel;
import com.dev_high.product.domain.ai.ChatResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SpringAiChatModel implements ChatModel {

//	private final ObjectProvider<ChatClient> chatClientProvider;
	private final ChatClient chatClient;
	private final PromptTemplate recommendTemplate;
	private final PromptTemplate testTemplate;


	// application.yml 또는 환경변수로 전달된 OpenAI API 키
	@Value("${spring.ai.openai.api-key:}")
	private String apiKey;


	@Override
	public ChatResult chat(ChatMessage message) {

//		ChatClient chatClient = chatClientProvider.getIfAvailable();
		if (chatClient == null || apiKey == null || apiKey.isBlank() || "changeme".equals(apiKey)) {
			log.warn("chat skipped: OpenAI API key not configured");
			return new ChatResult("OpenAI API key not configured", Map.of("source", "fallback"));
		}

		//템플릿에 질문을 주입하고 동기 호출 수행
		log.info("chat request: question='{}', context='{}'",
				message.value(),
				message.context());
		Prompt prompt = recommendTemplate.create(Map.of(
				"question", message.value(),
				"context", message.context()
		));
		ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
		Generation generation = response.getResult();

		Map<String, Object> metadata = new HashMap<>();
		response.getMetadata().entrySet()
				.forEach(entry -> metadata.put(entry.getKey(), entry.getValue()));
		String content = generation.getOutput().getText();
		log.info("chat response: content='{}', metadata={}", content, metadata);
		return new ChatResult(content, metadata);
	}


	@Override
	public ChatResult testChat(ChatMessage message) {

//		ChatClient chatClient = chatClientProvider.getIfAvailable();
		if (chatClient == null || apiKey == null || apiKey.isBlank() || "changeme".equals(apiKey)) {
			log.warn("testChat skipped: OpenAI API key not configured");
			return new ChatResult("OpenAI API key not configured", Map.of("source", "fallback"));
		}

		//템플릿에 질문을 주입하고 동기 호출 수행
		log.info("testChat request: question='{}'", message.value());
		Prompt prompt = testTemplate.create(Map.of(
				"question", message.value()
		));

		ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
		Generation generation = response.getResult();

		Map<String, Object> metadata = new HashMap<>();
		response.getMetadata().entrySet()
				.forEach(entry -> metadata.put(entry.getKey(), entry.getValue()));

		String content = generation.getOutput().getText();
		log.info("testChat response: content='{}', metadata={}", content, metadata);

		return new ChatResult(content, metadata);
	}


	@Override
	public Flux<String> stream(ChatMessage message) {
		// 스트리밍도 동일하게 키/빈 검사를 거쳐 SSE용 토큰 Flux 반환
//		ChatClient chatClient = chatClientProvider.getIfAvailable();
		if (chatClient == null || apiKey == null || apiKey.isBlank() || "changeme".equals(apiKey)) {
			return Flux.just("OpenAI API key not configured");
		}
		Prompt prompt = recommendTemplate.create(Map.of(
				"question", message.value(),
				"context", message.context()
		));
		return chatClient.prompt(prompt)
				.stream()
				.content();
	}
}
