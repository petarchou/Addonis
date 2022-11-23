package com.final_project.addonis.services;

import com.final_project.addonis.models.Tag;
import com.final_project.addonis.models.User;
import com.final_project.addonis.repositories.contracts.TagRepository;
import com.final_project.addonis.services.contracts.TagService;
import com.final_project.addonis.utils.exceptions.DuplicateEntityException;
import com.final_project.addonis.utils.exceptions.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
        Optional<Tag> tag = tagRepository.findById(tagId);

        if(tag.isPresent()) {
            return tag.get();
        }
        throw new EntityNotFoundException("Tag", tagId);
    }

    @Override
    public Tag getTagByName(String name) {
        Optional<Tag> tag  = tagRepository.findByName(name);
        if(tag.isEmpty()) {
            throw new EntityNotFoundException("Tag", "name", name);
        }
        return tag.get();
    }

    @Override
    public Tag create(Tag tag) {
        checkName(tag.getName());
        return tagRepository.saveAndFlush(tag);
    }

    @Override
    public Tag update(Tag tag, User user) {
        checkName(tag.getName());
        return tagRepository.save(tag);
    }

    @Override
    public Tag delete(int id, User user) {
        tagRepository.deleteById(id);
        return getTagById(id);
    }

    private void checkName(String name) {
        if (tagRepository.existsByName(name)) {
            throw new DuplicateEntityException("Tag", "name", name);
        }
    }
}
