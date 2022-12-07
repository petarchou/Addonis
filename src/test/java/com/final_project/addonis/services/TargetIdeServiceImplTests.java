package com.final_project.addonis.services;

import com.final_project.addonis.models.TargetIde;
import com.final_project.addonis.repositories.contracts.TargetIdeRepository;
import com.final_project.addonis.utils.exceptions.DuplicateEntityException;
import com.final_project.addonis.utils.exceptions.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TargetIdeServiceImplTests {
    @Mock
    private TargetIdeRepository targetIdeRepository;
    @InjectMocks
    private TargetIdeServiceImpl targetIdeServiceImp;

    @Test
    public void getAll_should_callRepository() {
        // Arrange
        List<TargetIde> repoList = List.of(new TargetIde(), new TargetIde());
        when(targetIdeRepository.findAll()).thenReturn(repoList);

        // Act
        List<TargetIde> serviceList = targetIdeServiceImp.getAll();

        // Assert
        assertAll(() -> assertSame(repoList, serviceList),
                () -> assertEquals(serviceList.size(), 2),
                () -> assertEquals(serviceList.get(0), repoList.get(0)),
                () -> assertEquals(serviceList.get(1), repoList.get(1)));
    }

    @Test
    public void getById_should_returnTargetIde_when_TargetIdeExist() {
        // Arrange
        TargetIde targetIde = new TargetIde();
        when(targetIdeRepository.findById(anyInt())).thenReturn(Optional.of(targetIde));

        // Act
        TargetIde serviceTargetIde = targetIdeServiceImp.getById(anyInt());

        // Assert
        assertEquals(targetIde, serviceTargetIde);
    }

    @Test
    public void getById_should_throwsException_when_TargetIdeNotExist() {
        // Arrange

        when(targetIdeRepository.findById(anyInt())).thenReturn(Optional.empty());

        //Act, Assert
        assertThrows(EntityNotFoundException.class,
                () -> targetIdeServiceImp.getById(anyInt()));
    }

    @Test
    public void create_should_createTargetIde_when_TargetIdeNameNotExist() {
        // Assert
        TargetIde targetIde = new TargetIde();
        targetIde.setName("testName");
        when(targetIdeRepository.existsByName(anyString())).thenReturn(false);
        when(targetIdeRepository.save(any())).thenReturn(null);

        // Act
        targetIdeServiceImp.create(targetIde);

        // Assert
        verify(targetIdeRepository, times(1)).save(any());
    }

    @Test
    public void create_should_throwsException_when_TargetIdeNameExist() {
        // Assert
        TargetIde targetIde = new TargetIde();
        targetIde.setName("testName");
        when(targetIdeRepository.existsByName(anyString())).thenReturn(true);

        // Assert
        assertThrows(DuplicateEntityException.class,
                () -> targetIdeServiceImp.create(targetIde));
    }

    @Test
    public void update_should_updateTargetIde_when_TargetIdeNameNotExist() {
        // Arrange
        TargetIde targetIde = new TargetIde();
        targetIde.setName("testName");
        when(targetIdeRepository.existsByName(anyString())).thenReturn(false);
        when(targetIdeRepository.saveAndFlush(any())).thenReturn(null);

        // Act
        targetIdeServiceImp.update(targetIde);

        // Assert
        verify(targetIdeRepository, times(1)).saveAndFlush(any());

    }

    @Test
    public void delete_should_deleteTargetIde() {
        // Arrange, Act
        targetIdeServiceImp.delete(new TargetIde());

        //Assert
        verify(targetIdeRepository, times(1)).delete(any());

    }

    @Test
    public void getByName_should_returnTargetIde_when_targetIdeExist(){
        // Arrange
        TargetIde targetIde = new TargetIde();
        when(targetIdeRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.of(targetIde));

        // Act
        TargetIde serviceTargetIde = targetIdeServiceImp.getByName(anyString());

        // Assert
        assertEquals(targetIde,serviceTargetIde);
    }

    @Test
    public void getByName_should_throwsException_when_targetIdeNotExist(){
        // Arrange
        when(targetIdeRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());

        // Act, Assert
        assertThrows(EntityNotFoundException.class,
                () -> targetIdeServiceImp.getByName(anyString()));


    }
}
