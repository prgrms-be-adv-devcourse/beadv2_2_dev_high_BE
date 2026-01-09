package com.dev_high.product.presentation.ai.dto;


import com.dev_high.product.application.ai.dto.ChatCommand;
import com.dev_high.product.application.dto.ProductCommand;
import jakarta.validation.constraints.NotBlank;

public record ChatRequest( String message) {
     public ChatCommand toCommand() {
        return new ChatCommand (message);
    }
}