package com.final_project.addonis.models.dtos;

import com.final_project.addonis.models.BinaryContent;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Setter
@Getter
public abstract class BaseAddonDtoOut {
    private int id;

    private String name;

    private String targetIde;

    private UserDto creator;

    private String description;

    private BinaryContent binaryContent;

    private String originUrl;

    private String uploadedDate;

    private String state;

    private Set<String> tags;

}
