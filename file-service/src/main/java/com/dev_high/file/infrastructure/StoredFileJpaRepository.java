package com.dev_high.file.infrastructure;

import com.dev_high.file.domain.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoredFileJpaRepository extends JpaRepository<StoredFile, String> {
}
