package com.final_project.addonis.models.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TagDto {

    @NotEmpty(message = "You should add at least 1 tag")
    private List<String> tagNames;

}
