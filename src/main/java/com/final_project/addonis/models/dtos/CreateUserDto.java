package com.final_project.addonis.models.dtos;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
public class CreateUserDto {
    @NotNull
    @Size(min = 8, max = 32)
    private String username;

    @NotNull
    @Size(min = 8, max = 32)
    private String password;
    private String confirmPassword;

    @Email
    private String email;

    @Size(min = 10, max = 10)
    private String phoneNumber;


}
