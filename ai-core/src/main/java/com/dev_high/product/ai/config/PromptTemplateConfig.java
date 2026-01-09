package com.dev_high.product.ai.config;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PromptTemplateConfig {

	@Bean
	public PromptTemplate recommendTemplate() {
		String template = """
		너는 살만한 경매 상품을 추천해주는 도우미야. 간결하게 답변해.
		사용자질문: {question}
		Context: {context}
		""";
		return new PromptTemplate(template);
	}


	@Bean
	public PromptTemplate testTemplate() {
		String template = """
		이건 테스트 템플릿이야.
		사용자 질문: {question}
		""";
		return new PromptTemplate(template);
	}
}