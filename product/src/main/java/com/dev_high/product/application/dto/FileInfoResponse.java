package com.dev_high.product.application.dto;

import java.time.LocalDateTime;

public record FileInfoResponse(
        String id,
        String fileName,
        String fileType,
        String filePath,
        String fileGroupId,
        String createdBy,
        LocalDateTime createdAt
) {
}

