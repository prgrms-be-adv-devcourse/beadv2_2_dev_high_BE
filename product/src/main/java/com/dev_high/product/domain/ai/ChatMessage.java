package com.dev_high.product.domain.ai;

public record ChatMessage(String value) {

	public ChatMessage {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException("Message must not be blank");
		}
		value = value.trim();
	}
}
