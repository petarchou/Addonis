package com.final_project.addonis.models.dtos;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.*;

@Getter
@Setter
public class CreateUserDto {

    @NotNull
    @Size(min = 2, max = 20, message = "Username must be between 2 and 20 characters long.")
    private String username;

    @NotNull
    @Size(min = 8,
    message = "Password must be at least 8 characters long.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+=-])[A-Za-z\\d!@#$%^&*()_+=-]{8,}$",
    message = "Password must contain a small letter, a capital letter, a number and a symbol.")
    private String password;

    private String confirmPassword;

    @Email(message = "Invalid email format", regexp = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:" +
            "[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}")
    private String email;

    @Pattern(regexp = "^\\d{10}$",
    message = "Phone must consist of exactly 10 digits.")
    private String phoneNumber;

}
