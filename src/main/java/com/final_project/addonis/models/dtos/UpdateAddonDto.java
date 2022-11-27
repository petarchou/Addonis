package com.final_project.addonis.models.dtos;

import com.final_project.addonis.models.State;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateAddonDto {

    private String name;

    private String description;

    private State state;

    private List<String> tags;
    private List<String> categories;

    public UpdateAddonDto() {
    }
}
