package com.final_project.addonis.utils.mappers;

import com.final_project.addonis.models.User;
import com.final_project.addonis.models.dtos.CreateUserDto;
import com.final_project.addonis.models.dtos.PasswordDto;
import com.final_project.addonis.models.dtos.UpdateUserDto;
import com.final_project.addonis.models.dtos.UserDto;
import com.final_project.addonis.utils.config.springsecurity.PasswordEncoder;
import com.final_project.addonis.utils.exceptions.PasswordNotMatchException;
import com.final_project.addonis.utils.helpers.SaveFileHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Component
public class UserMapper {
    private static final String DEFAULT_AVATAR = "/assets/avatars/default/OIP.png";
    private final static String INVALID_CONFIRMATION = "Invalid password confirmation";
    private final static String INVALID_VERIFICATION_PASSWORD = "Verification password does not match";
    private final BCryptPasswordEncoder passwordEncoder;
    private final SaveFileHelper fileHelper;

    public UserMapper(PasswordEncoder passwordEncoder,
                      SaveFileHelper saveFile) {
        this.passwordEncoder = passwordEncoder.getBCryptPasswordEncoder();
        this.fileHelper = saveFile;
    }

    public UserDto toDto(User user) {

        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setPhoneNumber(user.getPhoneNumber());
        userDto.setPhotoUrl(user.getPhotoUrl());

        return userDto;
    }

    public User fromCreateDto(CreateUserDto createUserDto) {

        if (!createUserDto.getPassword()
                .equals(createUserDto.getConfirmPassword())) {
            throw new PasswordNotMatchException(INVALID_CONFIRMATION);
        }

        User user = new User();
        user.setUsername(createUserDto.getUsername());
        user.setEmail(createUserDto.getEmail());
        user.setPhoneNumber(createUserDto.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(createUserDto.getPassword()));
        user.setPhotoUrl(DEFAULT_AVATAR);

        return user;
    }

    public User fromUpdateDto(UpdateUserDto updateUserDto, User user) {
        user.setEmail(updateUserDto.getEmail());
        user.setPhoneNumber(updateUserDto.getPhoneNumber());
        return user;
    }

    public User fromPasswordDto(User user, PasswordDto passwordDto) {

        if (!passwordEncoder.matches(passwordDto.getOldPassword()
                , user.getPassword())) {
            throw new PasswordNotMatchException(INVALID_VERIFICATION_PASSWORD);
        }

        if (!passwordDto.getNewPassword()
                .equals(passwordDto.getConfirmNewPassword())) {
            throw new PasswordNotMatchException(INVALID_CONFIRMATION);
        }

        user.setPassword(passwordEncoder.encode(passwordDto.getNewPassword()));
        return user;
    }

    public User changePhoto(User user, MultipartFile multipartFile) {

        String fileName = StringUtils.cleanPath(Objects
                .requireNonNull(multipartFile.getOriginalFilename()));
        String uploadDir = "src/main/resources/static/assets/avatars/" + user.getUsername();
        fileHelper.saveFile(uploadDir, fileName, multipartFile);
        user.setPhotoUrl("/assets/avatars/" + user.getUsername() + "/" + fileName);

        return user;
    }
}
