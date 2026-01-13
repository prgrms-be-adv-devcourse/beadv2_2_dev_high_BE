package com.dev_high.product.ai.domain;

// value는 질문, context는 데이터값
public record ChatMessage(String value, String context) {

	public ChatMessage {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException("Message must not be blank");
		}
		value = value.trim();
		if (context == null) {
			context = "";
		} else {
			context = context.trim();
		}
	}
}
