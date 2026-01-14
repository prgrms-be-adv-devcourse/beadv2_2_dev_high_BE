package com.dev_high.product.ai.infrastructure;

import com.dev_high.product.ai.domain.ChatMessage;
import com.dev_high.product.ai.dto.DraftTonePreset;
import com.dev_high.product.ai.dto.ProductDetailDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.AdvisorParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class SpringAiChatModel {

	private final ChatClient chatClient;

    private final PromptTemplate imageToDetailWithCategoryTemplate;


	public SpringAiChatModel(
			ChatClient chatClient,
			@Qualifier("imageToDetailWithCategoryTemplate") PromptTemplate imageToDetailWithCategoryTemplate
            ) {
		this.chatClient = chatClient;
        this.imageToDetailWithCategoryTemplate=imageToDetailWithCategoryTemplate;
	}

    public ProductDetailDto chatProductDetail(
            MultipartFile[] files,
            String categoryOptions,   // ✅ 호출자가 만들어서 넘김
            Integer retryCount
    ) {


        String systemText = imageToDetailWithCategoryTemplate.render(Map.of(
                "categoryOptions", categoryOptions
        ));

        String addContext =DraftTonePreset.buildExtraContext(retryCount);
        String userText = "첨부된 여러 장의 이미지를 종합해서, 위 필드 목록을 만족하는 JSON 한 개 객체만 출력해줘.\n추가 컨텍스트: " + addContext;


        try {
            return callEntity(systemText, userText, files);
        } catch (Exception e) {
            // 1회 재시도
            String retryText = userText + "\n\n반드시 유효한 JSON 한 개 객체만 다시 출력해줘. 문자열 따옴표/괄호를 빠뜨리지 마.";
            return callEntity(systemText, retryText, files);
        }
    }

    private ProductDetailDto callEntity(String systemText, String userText, MultipartFile[] files) {
        return chatClient.prompt()
                .advisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)
                .system(systemText)
                .user(u -> {
                    u.text(userText);
                    for (MultipartFile f : files) {
                        validateImage(f);
                        String mime = (f.getContentType() == null || f.getContentType().isBlank()) ? "image/jpeg" : f.getContentType();
                        u.media(MimeTypeUtils.parseMimeType(mime), f.getResource());
                    }
                })
                .call()
                .entity(ProductDetailDto.class);
    }


    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("file is empty");
        String ct = file.getContentType();
        if (ct == null || !ct.startsWith("image/")) {
            throw new IllegalArgumentException("only image/* allowed. contentType=" + ct);
        }
    }


}
