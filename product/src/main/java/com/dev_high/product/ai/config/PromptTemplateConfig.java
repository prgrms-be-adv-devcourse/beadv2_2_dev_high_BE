package com.dev_high.product.ai.config;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PromptTemplateConfig {


	@Bean
	public PromptTemplate recommendTemplate() {
		String template = """
		너는 경매참여할 상품을 찾아주는 도우미야. 간결하게 답변해.
		사용자 질문: {question}
		Context: {context}
		""";
		return new PromptTemplate(template);
	}


	@Bean
	public PromptTemplate testTemplate() {
		String template = """
		너는 쇼핑 도우미야. 간결하게 답변해.
		사용자 질문: {question}
		""";
		return new PromptTemplate(template);
	}

}
