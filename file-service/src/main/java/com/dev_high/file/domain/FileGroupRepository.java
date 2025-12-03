package com.dev_high.file.domain;

import java.util.Optional;

public interface FileGroupRepository {
    FileGroup save(FileGroup fileGroup);

    Optional<FileGroup> findById(String id);
}
