package com.dev_high.product.domain;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {

    Category save(Category category);

    Optional<Category> findById(String id);

    List<Category> findAll();
}

