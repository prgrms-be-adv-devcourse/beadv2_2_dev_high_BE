package com.dev_high.product.infraStructure;

import java.util.List;

import com.dev_high.product.domain.StoredFile;
import com.dev_high.product.domain.StoredFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StoredFileRepositoryAdapter implements StoredFileRepository {

    private final StoredFileJpaRepository storedFileJpaRepository;

    @Override
    public StoredFile save(StoredFile storedFile) {
        return storedFileJpaRepository.save(storedFile);
    }

    @Override
    public List<StoredFile> findByFileGroupId(String fileGroupId) {
        return storedFileJpaRepository.findByFileGroupId(fileGroupId);
    }
}
