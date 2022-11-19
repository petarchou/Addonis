package com.final_project.addonis.models.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {
    private int id;
    private String username;
    private String email;
    private String phoneNumber;
    private String photoUrl;

}
