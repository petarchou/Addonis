package com.final_project.addonis.models.dtos;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class EmailDto {

    @Email(message = "Invalid email format", regexp = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:" +
            "[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}")
    private String email;

    private boolean isSent;
}
