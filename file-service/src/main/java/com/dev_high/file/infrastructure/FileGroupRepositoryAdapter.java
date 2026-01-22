package com.dev_high.file.infrastructure;

import com.dev_high.file.domain.FileGroup;
import com.dev_high.file.domain.FileGroupRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FileGroupRepositoryAdapter implements FileGroupRepository {

    private final FileGroupJpaRepository fileGroupJpaRepository;

    @Override
    public FileGroup save(FileGroup fileGroup) {
        return fileGroupJpaRepository.save(fileGroup);
    }

    @Override
    public Optional<FileGroup> findById(String id) {
        return fileGroupJpaRepository.findById(id);
    }
}
