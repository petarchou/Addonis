package com.final_project.addonis.models.dtos;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class LoginDto {

    @NotNull
    private String username;

    @NotNull
    private String password;
}
