package com.dev_high.product.application;

import com.dev_high.product.domain.Category;
import com.dev_high.product.domain.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }


    public String categoryOptionsText() {
        // 예: "CAT001: 전자·디지털\nCAT002: 생활·가전\n..."
        return getCategories().stream()
                .sorted(Comparator.comparing(Category::getId))
                .map(c -> c.getId() + " | " + c.getCategoryName())
                .collect(Collectors.joining("\n"));
    }
}
