package com.final_project.addonis.repositories.contracts;

import com.final_project.addonis.models.User;

import java.util.List;
import java.util.Map;


public interface UserRepositoryCustom {
    List<User> findAllUsersByFilteringAndSorting(String keyword, Map<String, String> predicate);
}
