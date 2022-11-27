package com.final_project.addonis.utils.mappers;

import com.final_project.addonis.models.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category fromCategoryName(String categoryName) {
        Category category = new Category();
        category.setName(categoryName);
        return category;
    }
}
