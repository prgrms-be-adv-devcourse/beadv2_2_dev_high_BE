package com.dev_high.product.infraStructure;

import com.dev_high.product.domain.FileGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileGroupJpaRepository extends JpaRepository<FileGroup, String> {
}

