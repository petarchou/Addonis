package com.final_project.addonis.services.contracts;

import com.final_project.addonis.models.User;

import java.io.UnsupportedEncodingException;
import java.util.List;

public interface UserService {

    List<User> getAll();

    User getById(int id);

    User create(User user, String siteUrl);

    User update(User user);

    User delete(User user);

    User getByUsername(String username);

    User getByEmail(String email);

    User getByPhone(String phone);

    void verifyUser(String tokenStr);

    User changeUserRole(int id, String roleName, String action);

    User changeBlockedStatus(int id, String action);

}
