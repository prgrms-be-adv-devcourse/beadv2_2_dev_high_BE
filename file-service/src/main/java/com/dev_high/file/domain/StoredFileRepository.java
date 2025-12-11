package com.dev_high.file.domain;

import java.util.List;

public interface StoredFileRepository {

    StoredFile save(StoredFile storedFile);

    List<StoredFile> findAll();
}
