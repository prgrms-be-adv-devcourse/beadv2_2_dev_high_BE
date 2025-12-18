package com.dev_high.auction.application.dto;

import java.time.LocalDateTime;

public record FileDto(String id,
                      String fileName,
                      String fileType,
                      String filePath,
                      String fileGroupId,
                      String createdBy,
                      LocalDateTime createdAt) {

}
