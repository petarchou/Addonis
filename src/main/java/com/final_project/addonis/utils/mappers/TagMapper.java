package com.final_project.addonis.utils.mappers;

import com.final_project.addonis.models.Tag;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {

    public Tag fromTagName(String tagName) {
        Tag tag = new Tag();
        tag.setName(tagName);
        return tag;
    }
}
