package com.final_project.addonis.services.contracts;

import com.final_project.addonis.models.BinaryContent;
import org.springframework.web.multipart.MultipartFile;

public interface BinaryContentService {

    BinaryContent store(MultipartFile file);

    void delete(BinaryContent content);
}
