package com.dev_high.file.presentation.dto;

import com.dev_high.file.application.dto.FileUploadCommand;

public record FileUploadRequest(String fileType, String productId) {
    public FileUploadCommand toCommand() {
        return new FileUploadCommand(
                fileType, productId
        );
    }
}
