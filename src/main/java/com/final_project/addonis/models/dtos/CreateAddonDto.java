package com.final_project.addonis.models.dtos;

import com.final_project.addonis.models.TargetIde;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
public class CreateAddonDto {

    @NotNull
    @Size(min = 3, max = 30, message = "Name must be between 3 and 30 symbols")
    private String name;

    @NotNull
    private TargetIde targetIde;

    @NotNull
    @Size(min = 32, max = 8192, message = "Description must be between 32 and 8192 symbols")
    private String description;

    private String originUrl;

    @NotEmpty(message = "Addon must have at least one tag")
    private List<String> tags;

    public CreateAddonDto() {
    }
}


