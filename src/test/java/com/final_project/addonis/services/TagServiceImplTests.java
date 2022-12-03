package com.final_project.addonis.services;

import com.final_project.addonis.models.Tag;
import com.final_project.addonis.repositories.contracts.TagRepository;
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
public class TagServiceImplTests {
    @Mock
    private TagRepository tagRepository;
    @InjectMocks
    private TagServiceImpl tagService;


    @Test
    public void getAll_should_returnAllExistingTags() {
        // Arrange
        List<Tag> mockTags = List.of(new Tag(), new Tag());
        when(tagRepository.findAll()).thenReturn(mockTags);

        // Act
        List<Tag> serviceTags = tagService.getAll();

        // Assert
        assertSame(mockTags, serviceTags);
    }

    @Test
    public void getTagById_should_returnTag_when_tagWhitIdExists() {
        // Arrange
        when(tagRepository.findById(anyInt())).thenReturn(Optional.of(new Tag()));

        // Act
        tagService.getTagById(1);

        // Assert
        verify(tagRepository, times(1)).findById(anyInt());
    }

    @Test
    public void getTagById_should_throwsException_when_tagWhitIdNotExists() {
        // Arrange
        when(tagRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Act, Assert
        assertThrows(EntityNotFoundException.class,
                () -> tagService.getTagById(1));
    }

    @Test
    public void create_should_createTag_when_tagNameNotExists() {
        // Arrange
        Tag mockTag = new Tag("test");
        when(tagRepository.saveAndFlush(any())).thenReturn(mockTag);
        when(tagRepository.existsByName(anyString())).thenReturn(false);

        // Act
        Tag serviceTag = tagService.create(mockTag);

        // Assert
        assertSame(mockTag, serviceTag);
    }

    @Test
    public void create_should_throwsException_when_tagNameExists() {
        // Arrange
        Tag mockTag = new Tag("test");
        when(tagRepository.existsByName(anyString())).thenReturn(true);

        // Act, Assert
        assertThrows(DuplicateEntityException.class,
                () -> tagService.create(mockTag));
    }

    @Test
    public void update_should_callRepository() {
        // Arrange
        when(tagRepository.saveAndFlush(any())).thenReturn(null);
        when(tagRepository.existsByName(anyString())).thenReturn(false);

        // Act
        tagService.update(new Tag("test"));

        //Assert
        verify(tagRepository, times(1)).saveAndFlush(any());
    }

    @Test
    public void delete_should_callRepository() {
        // Arrange
        when(tagRepository.findById(anyInt())).thenReturn(Optional.of(new Tag()));

        // Act
        tagService.delete(anyInt());

        //Assert
        verify(tagRepository, times(1)).deleteById(any());
    }

}
