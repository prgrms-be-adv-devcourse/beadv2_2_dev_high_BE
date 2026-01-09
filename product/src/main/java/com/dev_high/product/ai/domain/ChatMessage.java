package com.dev_high.product.ai.domain;

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

	public ChatMessage(String value) {
		this(value, "");
	}
}
