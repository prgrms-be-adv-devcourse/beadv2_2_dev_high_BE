package com.dev_high.product.domain;

import java.util.List;

public interface StoredFileRepository {

    StoredFile save(StoredFile storedFile);

    List<StoredFile> findByFileGroupId(String fileGroupId);
}
