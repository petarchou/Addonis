package com.final_project.addonis.repositories.contracts;

import com.final_project.addonis.models.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;


public interface UserRepositoryCustom {
    List<User> findAllUsersByFilteringAndSorting(String keyword,
                                                 Optional<String> filterBy,
                                                 String sortOrDefault,
                                                 boolean descOrder,
                                                 int page, int size);
}
