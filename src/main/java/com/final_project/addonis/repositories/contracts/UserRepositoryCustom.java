package com.final_project.addonis.repositories.contracts;

import com.final_project.addonis.models.User;

import java.util.List;
import java.util.Optional;


public interface UserRepositoryCustom {
    List<User> findAllUsersByFilteringAndSorting(Optional<String> keyword,
                                                 Optional<String> filterBy,
                                                 String sortOrDefault,
                                                 boolean ascending,
                                                 int page, int size);
}
