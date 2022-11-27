package com.final_project.addonis.services;

import com.final_project.addonis.models.Category;
import com.final_project.addonis.repositories.contracts.CategoryRepository;
import com.final_project.addonis.services.contracts.CategoryService;
import com.final_project.addonis.utils.exceptions.DuplicateEntityException;
import com.final_project.addonis.utils.exceptions.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
        Optional<Category> category = categoryRepository.findById(id);

        if (category.isPresent()) {
            return category.get();
        }
        throw new EntityNotFoundException("Category", id);
    }

    @Override
    public Category getCategoryByName(String name) {
        Optional<Category> category = categoryRepository.findByName(name);
        if (category.isPresent()) {
            return category.get();
        }
        throw new EntityNotFoundException("Category", "name", name);
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

    private void verifyIsUniqueName(Category category) {
        boolean exist = true;
        Category existingCategory = new Category();

        try {
            existingCategory = getCategoryByName(category.getName());
        } catch (EntityNotFoundException e) {
            exist = false;
        }

        if (exist && !category.equals(existingCategory)) {
            throw new DuplicateEntityException("Category", "name", category.getName());
        }
    }
}