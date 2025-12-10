package com.dev_high.file.domain;

import com.dev_high.common.annotation.CustomGeneratedId;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "file", schema = "file")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoredFile {

    @Id
    @Column(length = 20)
    @CustomGeneratedId(method = "file")
    private String id;

    @Column(name = "file_path", length = 255, nullable = false)
    private String filePath;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Builder
    private StoredFile(String filePath, String fileType, String fileName, String createdBy) {
        this.filePath = filePath;
        this.fileType = fileType;
        this.fileName = fileName;
        this.createdBy = createdBy;
        this.updatedBy = createdBy;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void updateAudit(String updaterId) {
        this.updatedBy = updaterId;
    }
}
