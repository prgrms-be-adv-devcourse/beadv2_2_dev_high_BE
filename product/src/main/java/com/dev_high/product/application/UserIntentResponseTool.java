package com.dev_high.product.application;

import com.dev_high.product.application.dto.UserIntentResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class UserIntentResponseTool {

    @Tool(
            name = "user_intent_response",
            description = "유저가 입력한 질문의 의도를 분석하여 의도와 의도분석내용을 반환합니다."
    )
    public UserIntentResponse response(
            @ToolParam(description = "의도 종류: GREETING, PRODUCT, SERVICE, OFF_TOPIC, ABUSIVE, NON_PRODUCT")
            String intent,
            @ToolParam(description = "해당 intent를 선택한 이유를 1-3 문장의 한국어로 응답한다.")
            String answer
    ){
        return new UserIntentResponse(intent, answer);
    }
}
