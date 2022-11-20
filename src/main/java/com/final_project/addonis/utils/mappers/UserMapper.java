package com.final_project.addonis.utils.mappers;

import com.final_project.addonis.models.User;
import com.final_project.addonis.models.dtos.CreateUserDto;
import com.final_project.addonis.models.dtos.UpdateUserDto;
import com.final_project.addonis.models.dtos.UserDto;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    private static final String DEFAULT_AVATAR = "/assets/avatars/default/OIP.png";

    public UserDto toDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setPhoneNumber(user.getPhoneNumber());
        userDto.setPhotoUrl(user.getPhotoUrl());

        return userDto;
    }

    public User fromDto(CreateUserDto createUserDto) {
        User user = new User();
        user.setUsername(createUserDto.getUsername());
        user.setEmail(createUserDto.getEmail());
        user.setPhoneNumber(createUserDto.getPhoneNumber());
        //add encoder here  and encode pass here?
        user.setPassword(createUserDto.getPassword());
        user.setPhotoUrl(DEFAULT_AVATAR);
        return user;
    }

    public User fromUpdateDto(UpdateUserDto updateUserDto, User user) {
        user.setEmail(updateUserDto.getEmail());
        user.setPhoneNumber(updateUserDto.getPhoneNumber());
        if(updateUserDto.getPhotoUrl()!=null)
            user.setPhotoUrl(updateUserDto.getPhotoUrl());
        return user;
    }
}
