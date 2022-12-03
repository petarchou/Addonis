package com.final_project.addonis.services;

import com.final_project.addonis.repositories.contracts.BinaryContentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BinaryContentServiceImplTests {
    @Mock
    private BinaryContentRepository repository;
    @InjectMocks
    private BinaryContentServiceImpl service;

    @Test
    public void store_should_createBinaryContent_when_FileIsNotEmpty() throws IOException {
        // Arrange
        MockMultipartFile mockFile = new MockMultipartFile("test", new byte[]{1, 2});
        when(repository.save(any())).thenReturn(null);

        // Act
        service.store(mockFile);

        // Assert
        verify(repository, times(1)).save(any());
    }

    @Test
    public void store_should_throwsException_when_FileIsEmpty(){
        // Arrange
        MockMultipartFile mockFile = new MockMultipartFile("test", new byte[0]);

        // Act, Assert
        assertThrows(IllegalArgumentException.class,
                () -> service.store(mockFile));
    }
}
