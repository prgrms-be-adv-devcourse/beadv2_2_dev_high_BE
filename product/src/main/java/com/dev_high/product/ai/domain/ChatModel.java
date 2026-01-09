package com.dev_high.product.ai.domain;

import reactor.core.publisher.Flux;


//repository 역할
public interface ChatModel {

	ChatResult chat(ChatMessage message);

	ChatResult testChat(ChatMessage message);

	Flux<String> stream(ChatMessage message);
}
