package com.dev_high.product.ai.domain;

//간편 호출용 인터페이스
public interface ChatModel {

	ChatResult chat(ChatMessage message);

}
