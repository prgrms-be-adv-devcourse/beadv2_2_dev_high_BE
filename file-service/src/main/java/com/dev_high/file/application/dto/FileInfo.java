package com.dev_high.file.application.dto;

import com.dev_high.file.domain.StoredFile;
import java.time.LocalDateTime;

public record FileInfo(
        String id,
        String fileName,
        String fileType,
        String filePath,
        String createdBy,
        LocalDateTime createdAt
) {

    public static FileInfo from(StoredFile storedFile) {
        return new FileInfo(
                storedFile.getId(),
                storedFile.getFileName(),
                storedFile.getFileType(),
                storedFile.getFilePath(),
                storedFile.getCreatedBy(),
                storedFile.getCreatedAt()
        );
    }
}
