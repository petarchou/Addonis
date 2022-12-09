package com.final_project.addonis.services.contracts;

import com.final_project.addonis.models.User;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface UserService {

    Page<User> getAll(Optional<String> keyword,
                      Optional<String> filterByField,
                      Optional<String> sortByField,
                      Optional<Boolean> order,
                      Optional<Integer> page,
                      Optional<Integer> size);

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


    void sendResetPasswordRequest(User user, String siteUrl);
}
