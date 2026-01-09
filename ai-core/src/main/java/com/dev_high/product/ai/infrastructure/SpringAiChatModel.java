package com.dev_high.product.ai.infrastructure;

import com.dev_high.product.ai.domain.ChatMessage;
import com.dev_high.product.ai.domain.ChatModel;
import com.dev_high.product.ai.domain.ChatResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class SpringAiChatModel implements ChatModel {

//	private final ObjectProvider<ChatClient> chatClientProvider;
	private final ChatClient chatClient;

	private final PromptTemplate recommendTemplate;


	@Value("${spring.ai.openai.api-key:}")
	private String apiKey;

	public SpringAiChatModel(
			ChatClient chatClient,
			@Qualifier("recommendTemplate") PromptTemplate recommendTemplate) {
		this.chatClient = chatClient;
		this.recommendTemplate = recommendTemplate;
	}


	@Override
	public ChatResult chat(ChatMessage message) {

//		ChatClient chatClient = chatClientProvider.getIfAvailable();
		if (chatClient == null || apiKey == null || apiKey.isBlank() || "changeme".equals(apiKey)) {
			log.warn("chat skipped: OpenAI API key not configured");
			return new ChatResult("OpenAI API key not configured", Map.of("source", "fallback"));
		}

		log.info("chat request: question='{}', context='{}'",
				message.value(),
				message.context());
		Prompt prompt = recommendTemplate.create(Map.of(
				"question", message.value(),
				"context", message.context()
		));
		log.info("chat prompt: {}", prompt);
		ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
		Generation generation = response.getResult();
		log.info("chat response raw: {}", response);

		Map<String, Object> metadata = new HashMap<>();
		response.getMetadata().entrySet()
				.forEach(entry -> metadata.put(entry.getKey(), entry.getValue()));
		String content = generation.getOutput().getText();
		log.info("chat response: content='{}', metadata={}", content, metadata);
		return new ChatResult(content, metadata);
	}

}
