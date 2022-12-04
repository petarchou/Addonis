package com.final_project.addonis.services.contracts;

import com.final_project.addonis.models.BinaryContent;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface BinaryContentService {

    BinaryContent store(MultipartFile file) throws IOException;

    void delete(BinaryContent content) throws IOException;
}
