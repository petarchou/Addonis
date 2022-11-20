package com.final_project.addonis.services.contracts;

import com.final_project.addonis.models.User;

import java.util.List;

public interface UserService {

    List<User> getAll();

    User getById(int id);

    User create(User user);

    User update(User user);

    User delete(User user);

    User getByUsername(String username);

}
