package com.dev_high.product.admin;

import com.dev_high.product.admin.dto.AiProductSpec;
import com.dev_high.product.admin.dto.AiProductSpecList;
import com.dev_high.product.admin.dto.ImagePromptResponse;
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class ProductAiTool {

    @Tool(
        name = "product_spec_list",
        description = "카테고리별 생성 요청에 맞는 상품 스펙 목록을 반환합니다."
    )
    public AiProductSpecList createSpecList(
        @ToolParam(description = "상품 스펙 목록")
        List<AiProductSpec> items
    ) {
        return new AiProductSpecList(items);
    }

    @Tool(
        name = "product_image_prompt",
        description = "상품 제목/설명을 바탕으로 이미지 생성 프롬프트를 반환합니다."
    )
    public ImagePromptResponse createImagePrompt(
        @ToolParam(description = "실사 이미지 생성을 위한 영어 프롬프트")
        String prompt
    ) {
        return new ImagePromptResponse(prompt);
    }
}
