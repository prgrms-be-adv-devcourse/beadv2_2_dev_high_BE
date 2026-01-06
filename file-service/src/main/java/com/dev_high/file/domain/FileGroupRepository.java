package com.dev_high.file.domain;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.file.application.dto.FileGroupResponse;

import java.util.List;
import java.util.Optional;

public interface FileGroupRepository {
    FileGroup save(FileGroup fileGroup);

    Optional<FileGroup> findById(String id);

    List<FileGroup> findByFileGroupIds(List<String> fileGroupId);
}
