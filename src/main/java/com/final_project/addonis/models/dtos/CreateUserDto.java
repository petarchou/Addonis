package com.final_project.addonis.models.dtos;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.*;

@Getter
@Setter
public class CreateUserDto {

    @NotNull
    @Size(min = 2, max = 20)
    private String username;

    @NotNull
    @Size(min = 8)
    @Pattern(regexp = "/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+=-])[A-Za-z\d!@#$%^&*()_+=-]{8,}$")
    private String password;
    private String confirmPassword;

    @Email
    private String email;

    @Size(min = 10, max = 10)
    @Pattern(regexp = "^\d{10}")
    private String phoneNumber;

}
