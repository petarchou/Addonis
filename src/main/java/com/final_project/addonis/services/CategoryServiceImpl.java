package com.final_project.addonis.services;

import com.final_project.addonis.models.Category;
import com.final_project.addonis.repositories.contracts.CategoryRepository;
import com.final_project.addonis.services.contracts.CategoryService;
import com.final_project.addonis.utils.exceptions.DuplicateEntityException;
import com.final_project.addonis.utils.exceptions.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Category getCategoryById(int id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category", id));
    }



    @Override
    public Category create(Category category) {
        verifyIsUniqueName(category);
        return categoryRepository.saveAndFlush(category);
    }

    @Override
    public Category update(Category category) {
        verifyIsUniqueName(category);
        return categoryRepository.saveAndFlush(category);
    }

    @Override
    public Category delete(int id) {
        categoryRepository.deleteById(id);
        return getCategoryById(id);
    }

    @Override
    public boolean existByName(String name) {
        return categoryRepository.existsByName(name);
    }

    private void verifyIsUniqueName(Category category) {
        if (existByName(category.getName())) {
            throw new DuplicateEntityException("Category", "name", category.getName());

        }
    }
}
