package com.final_project.addonis.services.contracts;

import com.final_project.addonis.models.Tag;
import com.final_project.addonis.models.User;

import java.util.List;

public interface TagService {

    List<Tag> getAll();

    Tag getTagById(int tagId);

    Tag create(Tag tag);

    Tag update(Tag tag, User user);

    Tag delete(int id, User user);

    Tag getTagByName(String name);
}
