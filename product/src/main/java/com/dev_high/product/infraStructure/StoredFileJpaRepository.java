package com.dev_high.product.infraStructure;


import com.dev_high.product.domain.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StoredFileJpaRepository extends JpaRepository<StoredFile, String> {
    List<StoredFile> findByFileGroupId(String fileGroupId);
}
