package com.dev_high.product.domain;


import java.util.List;
import java.util.Optional;

public interface FileGroupRepository {
    FileGroup save(FileGroup fileGroup);

    Optional<FileGroup> findById(String id);

    List<FileGroup> findByFileGroupIds(List<String> fileGroupId);
}
