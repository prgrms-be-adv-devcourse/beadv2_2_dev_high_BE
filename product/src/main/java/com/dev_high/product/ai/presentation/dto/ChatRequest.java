package com.dev_high.product.ai.presentation.dto;


import com.dev_high.product.ai.application.dto.ChatCommand;

public record ChatRequest( String message) {
     public ChatCommand toCommand() {
        return new ChatCommand (message);
    }
}