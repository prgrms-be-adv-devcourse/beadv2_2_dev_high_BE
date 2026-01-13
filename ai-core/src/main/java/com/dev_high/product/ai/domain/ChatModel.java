package com.dev_high.product.ai.domain;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Stream;

//간편 호출용 인터페이스
public interface ChatModel {

	// 필수 파라미터: systemText, PromptTemplate template
	// 선택 파라미터: userText, MulitipartFile[]

	ChatResult<String> chat(
			String systemText,
			PromptTemplate template
	);

	ChatResult<String> chat(
			String systemText,
			PromptTemplate template,
			String userText
	);

	ChatResult<String> chat(
			String systemText,
			PromptTemplate template,
			MultipartFile[] multipartFiles
	);

	ChatResult<String> chat(
			String systemText,
			PromptTemplate template,
			String userText,
			MultipartFile[] multipartFiles
	);

	<T> ChatResult<T> chat(
			String systemText,
			PromptTemplate template,
			Class<T> responseType
	);

	<T> ChatResult<T> chat(
			String systemText,
			PromptTemplate template,
			Class<T> responseType,
			String userText
	);

	<T> ChatResult<T> chat(
			String systemText,
			PromptTemplate template,
			Class<T> responseType,
			MultipartFile[] multipartFiles
	);

	<T> ChatResult<T> chat(
			String systemText,
			PromptTemplate template,
			Class<T> responseType,
			String userText,
			MultipartFile[] multipartFiles
	);

	ChatResult<Stream<String>> chatStream(String message, PromptTemplate template); // 또는 Stream<String>
}
