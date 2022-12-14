package com.final_project.addonis.models.dtos;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
public class UpdateUserDto {

    @Email(message = "Invalid email format", regexp = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:" +
            "[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}")
    private String email;

    @Size(min = 10, max = 10)
    @Pattern(regexp = "^\\d{10}")
    private String phoneNumber;

}
