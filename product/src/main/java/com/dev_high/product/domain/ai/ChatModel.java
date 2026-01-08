package com.dev_high.product.domain.ai;

import reactor.core.publisher.Flux;

public interface ChatModel {

	ChatResult chat(ChatMessage message);

	Flux<String> stream(ChatMessage message);
}
