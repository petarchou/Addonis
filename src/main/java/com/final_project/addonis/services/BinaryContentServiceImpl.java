package com.final_project.addonis.services;

import com.final_project.addonis.models.BinaryContent;
import com.final_project.addonis.repositories.contracts.BinaryContentRepository;
import com.final_project.addonis.services.contracts.BinaryContentService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

@Service
public class BinaryContentServiceImpl implements BinaryContentService {

    private final BinaryContentRepository binaryContentRepository;

    public BinaryContentServiceImpl(BinaryContentRepository binaryContentRepository) {
        this.binaryContentRepository = binaryContentRepository;
    }

    @Override
    public BinaryContent store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("You must add file to your Add-on");
        }
        try {

            String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            Optional<BinaryContent> optional = binaryContentRepository.findByData(file.getBytes());

            BinaryContent binaryContent = new BinaryContent(fileName, file.getBytes());

            return optional.orElseGet(() -> binaryContentRepository.save(binaryContent));
        } catch (IOException e) {
            throw new IllegalArgumentException("File read error");
        }
    }

    @Override
    public void delete(BinaryContent binaryContent) {
        binaryContentRepository.delete(binaryContent);
    }
}
