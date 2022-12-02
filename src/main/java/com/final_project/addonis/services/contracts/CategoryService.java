package com.final_project.addonis.services.contracts;

import com.final_project.addonis.models.Category;

import java.util.List;

public interface CategoryService {

    List<Category> getAll();

    Category getCategoryById(int id);

    Category create(Category category);

    Category update(Category category);

    Category delete(int id);

    boolean existByName(String name);
}
