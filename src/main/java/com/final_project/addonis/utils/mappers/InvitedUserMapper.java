package com.final_project.addonis.utils.mappers;

import com.final_project.addonis.models.InvitedUser;
import org.springframework.stereotype.Component;

@Component
public class InvitedUserMapper {

    public InvitedUser fromEmail(String email) {
        InvitedUser user = new InvitedUser();
        user.setEmail(email);
        return user;
    }
}
