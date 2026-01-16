package com.dev_high.product.application;

import com.dev_high.product.application.dto.ProductRecommendationToolResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class ProductRecommendationTool {

    @Tool(
        name = "product_recommendation_response",
        description = "Return product chatbot intent and answer.",
        returnDirect = true
    )
    public ProductRecommendationToolResponse respond(
        @ToolParam(description = "Intent category: GREETING, PRODUCT, SERVICE, OFF_TOPIC, ABUSIVE")
        String intent,
        @ToolParam(description = "Response in Korean, 1-3 sentences.")
        String answer
    ) {
        return new ProductRecommendationToolResponse(intent, answer);
    }
}
