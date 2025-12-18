package com.dev_high.file.application.dto;

import java.util.List;

public record FileGroupResponse(
        String fileGroupId,
        List<FileInfo> files
) {
}

