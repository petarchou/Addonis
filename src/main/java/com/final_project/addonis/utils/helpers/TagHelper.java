package com.final_project.addonis.utils.helpers;

import com.final_project.addonis.models.Tag;
import com.final_project.addonis.repositories.contracts.TagRepository;
import org.springframework.stereotype.Component;

@Component
public class TagHelper {
    private final TagRepository repository;

    public TagHelper(TagRepository repository) {
        this.repository = repository;
    }

    public Tag fromTagName(String tagName) {
        return repository.findByName(tagName)
                .orElse(new Tag(tagName));
    }
}
