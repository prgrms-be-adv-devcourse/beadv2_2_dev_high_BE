package com.dev_high.product.infraStructure;

import com.dev_high.product.domain.Category;
import com.dev_high.product.domain.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryAdapter implements CategoryRepository {

    private final CategoryJpaRepository categoryJpaRepository;

    @Override
    public Category save(Category category) {
        return categoryJpaRepository.save(category);
    }

    @Override
    public Optional<Category> findById(String id) {
        return categoryJpaRepository.findById(id);
    }

    @Override
    public List<Category> findAll() {
        return categoryJpaRepository.findAll();
    }
}
