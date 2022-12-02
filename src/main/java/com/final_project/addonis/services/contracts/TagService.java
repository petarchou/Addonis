package com.final_project.addonis.services.contracts;

import com.final_project.addonis.models.Tag;

import java.util.List;

public interface TagService {

    List<Tag> getAll();

    Tag getTagById(int tagId);

    Tag create(Tag tag);

    Tag update(Tag tag);

    Tag delete(int id);
}
