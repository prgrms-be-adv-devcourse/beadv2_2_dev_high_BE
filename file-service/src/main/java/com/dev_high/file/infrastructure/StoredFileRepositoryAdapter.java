package com.dev_high.file.infrastructure;

import com.dev_high.file.domain.StoredFile;
import com.dev_high.file.domain.StoredFileRepository;
import java.util.List;
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
    public List<StoredFile> findAll() {
        return storedFileJpaRepository.findAll();
    }
}
