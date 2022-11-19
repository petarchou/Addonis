package com.final_project.addonis.models.dtos;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Getter
@Setter
public class UpdateUserDto {

    @Email
    private String email;
    @Size(min = 10, max = 10)
    private String phoneNumber;
    private String photoUrl;
}
