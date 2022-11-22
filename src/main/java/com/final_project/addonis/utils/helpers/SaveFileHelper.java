package com.final_project.addonis.utils.helpers;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
@Component
public class SaveFileHelper {
    private static final String INVALID_IMAGE = "Could not save image file: ";

    public void saveFile(String uploadDir, String fileName,
                                MultipartFile multipartFile) {

        Path uploadPath = Paths.get(uploadDir);
        try (InputStream inputStream = multipartFile.getInputStream()) {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioe) {
            throw new UnsupportedOperationException(INVALID_IMAGE + fileName);
        }

    }
}
