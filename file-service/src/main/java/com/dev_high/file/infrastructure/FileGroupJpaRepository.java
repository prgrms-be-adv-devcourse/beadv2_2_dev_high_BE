package com.dev_high.file.infrastructure;

import com.dev_high.file.domain.FileGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileGroupJpaRepository extends JpaRepository<FileGroup, String> {
}

