package com.final_project.addonis.models.dtos;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CreateAddonDto extends BaseAddonDtoIn {

    @NotEmpty(message = "Addon must have at least one tag")
    private List<String> tags = new ArrayList<>();

    public CreateAddonDto() {
    }
}


