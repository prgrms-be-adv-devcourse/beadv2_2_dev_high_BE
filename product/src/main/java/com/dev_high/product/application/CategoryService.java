package com.dev_high.product.application;

import com.dev_high.product.application.dto.CategoryInfo;
import com.dev_high.product.domain.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryInfo> getCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryInfo::from)
                .toList();
    }
}
