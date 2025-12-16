package com.dev_high.file.infrastructure;

import com.dev_high.file.domain.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StoredFileJpaRepository extends JpaRepository<StoredFile, String> {
    List<StoredFile> findByProductId(String productId);

    void deleteByProductId(String productId);
}
