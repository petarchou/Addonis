package com.final_project.addonis.models.dtos;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
public abstract class BaseAddonDtoIn {

    @NotNull
    @Size(min = 3, max = 30, message = "Name must be between 3 and 30 symbols")
    private String name;

    @NotNull
    private String targetIde;

    @NotNull
    @Size(min = 32, max = 8192, message = "Description must be between 32 and 8192 symbols")
    private String description;

    private String originUrl;

}
