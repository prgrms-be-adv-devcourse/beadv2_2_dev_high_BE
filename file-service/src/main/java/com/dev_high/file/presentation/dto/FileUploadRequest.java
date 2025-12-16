package com.dev_high.file.presentation.dto;

import com.dev_high.file.application.dto.FileUploadCommand;

public record FileUploadRequest(String fileType) {
    public FileUploadCommand toCommand(String productId, String userId) {
        return new FileUploadCommand(
                fileType, productId, userId
        );
    }
}
