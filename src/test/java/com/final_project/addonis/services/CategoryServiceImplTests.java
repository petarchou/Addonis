package com.final_project.addonis.services;

import com.final_project.addonis.models.Category;
import com.final_project.addonis.repositories.contracts.CategoryRepository;
import com.final_project.addonis.utils.exceptions.DuplicateEntityException;
import com.final_project.addonis.utils.exceptions.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplTests {
    @Mock
    private CategoryRepository categoryRepository;
    @InjectMocks
    private CategoryServiceImpl categoryService;


    @Test
    public void getAll_should_returnAllExistingCategories() {
        // Arrange
        List<Category> mockCategories = List.of(new Category(), new Category());
        when(categoryRepository.findAll()).thenReturn(mockCategories);

        // Act
        List<Category> categoryServiceAll = categoryService.getAll();

        // Assert
        assertSame(mockCategories, categoryServiceAll);
    }

    @Test
    public void getCategoryById_should_returnCategory_when_categoryWhitIdExists() {
        // Arrange
        when(categoryRepository.findById(anyInt())).thenReturn(Optional.of(new Category()));

        // Act
        categoryService.getCategoryById(1);

        // Assert
        verify(categoryRepository, times(1)).findById(anyInt());
    }

    @Test
    public void getCategoryById_should_throwsException_when_categoryWhitIdNotExists() {
        // Arrange
        when(categoryRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Act, Assert
        assertThrows(EntityNotFoundException.class,
                () -> categoryService.getCategoryById(1));
    }

    @Test
    public void create_should_createCategory_when_categoryNameNotExists() {
        // Arrange
        Category mockCategory = new Category("test");
        when(categoryRepository.existsByName(anyString())).thenReturn(false);
        when(categoryRepository.saveAndFlush(any())).thenReturn(mockCategory);

        // Act
        Category serviceCategory = categoryService.create(mockCategory);

        // Assert
        assertSame(mockCategory, serviceCategory);
    }

    @Test
    public void create_should_throwsException_when_categoryNameExists() {
        // Arrange
        Category mockCategory = new Category("test");
        when(categoryRepository.existsByName(anyString())).thenReturn(true);

        // Act, Assert
        assertThrows(DuplicateEntityException.class,
                () -> categoryService.create(mockCategory));
    }

    @Test
    public void update_should_callRepository() {
        // Arrange
        when(categoryRepository.saveAndFlush(any())).thenReturn(null);
        when(categoryRepository.existsByName(anyString())).thenReturn(false);

        // Act
        categoryService.update(new Category("test"));

        //Assert
        verify(categoryRepository, times(1)).saveAndFlush(any());
    }

    @Test
    public void delete_should_callRepository() {
        // Arrange
        when(categoryRepository.findById(anyInt()))
                .thenReturn(Optional.of(new Category()));

        // Act
        categoryService.delete(anyInt());

        //Assert
        verify(categoryRepository, times(1)).deleteById(any());
    }

}
