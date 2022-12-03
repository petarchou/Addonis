package com.final_project.addonis.services;

import com.final_project.addonis.models.Tag;
import com.final_project.addonis.repositories.contracts.TagRepository;
import com.final_project.addonis.services.contracts.TagService;
import com.final_project.addonis.utils.exceptions.DuplicateEntityException;
import com.final_project.addonis.utils.exceptions.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    public TagServiceImpl(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    public List<Tag> getAll() {
        return tagRepository.findAll();
    }

    @Override
    public Tag getTagById(int tagId) {
        return tagRepository.findById(tagId).orElseThrow(
                () -> new EntityNotFoundException("Tag", tagId));
    }

    @Override
    public Tag create(Tag tag) {
        verifyIsUniqueName(tag);
        return tagRepository.saveAndFlush(tag);
    }

    @Override
    public Tag update(Tag tag) {
        verifyIsUniqueName(tag);
        return tagRepository.saveAndFlush(tag);
    }

    @Override
    public Tag delete(int id) {
        tagRepository.deleteById(id);
        return getTagById(id);
    }

    private void verifyIsUniqueName(Tag tag) {
        if (tagRepository.existsByName(tag.getName())) {
            throw new DuplicateEntityException("Tag", "name", tag.getName());
        }
    }
}