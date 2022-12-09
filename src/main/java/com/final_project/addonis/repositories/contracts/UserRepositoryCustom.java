package com.final_project.addonis.repositories.contracts;

import com.final_project.addonis.models.User;
import org.springframework.data.domain.Page;

import java.util.Optional;


public interface UserRepositoryCustom {
    Page<User> findAllUsersByFilteringAndSorting(Optional<String> keyword,
                                                 Optional<String> filterBy,
                                                 String sortOrDefault,
                                                 boolean ascending,
                                                 int page, int size);
}
