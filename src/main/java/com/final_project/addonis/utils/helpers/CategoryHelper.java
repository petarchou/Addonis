package com.final_project.addonis.utils.helpers;

import com.final_project.addonis.models.Category;
import com.final_project.addonis.repositories.contracts.CategoryRepository;
import org.springframework.stereotype.Component;

@Component
public class CategoryHelper {
    private final CategoryRepository repository;

    public CategoryHelper(CategoryRepository repository) {
        this.repository = repository;
    }

    public Category fromCategoryName(String categoryName) {
        return repository.findByName(categoryName)
                .orElse(new Category(categoryName));
    }
}
