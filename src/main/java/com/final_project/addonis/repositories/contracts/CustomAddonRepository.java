package com.final_project.addonis.repositories.contracts;

import com.final_project.addonis.models.Addon;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CustomAddonRepository {

    List<Addon> findAllAddonsByFilteringAndSorting(String keyword,
                                                   Optional<String> filterBy,
                                                   String sortOrDefault,
                                                   boolean DescOrder,
                                                   int page,
                                                   int size);
}
