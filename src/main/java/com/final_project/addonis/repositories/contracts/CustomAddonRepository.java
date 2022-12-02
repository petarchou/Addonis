package com.final_project.addonis.repositories.contracts;

import com.final_project.addonis.models.Addon;

import java.util.List;
import java.util.Optional;

public interface CustomAddonRepository {

    List<Addon> findAllAddonsByFilteringAndSorting(Optional<String> keyword,
                                                   Optional<String> targetIde,
                                                   Optional<String> category,
                                                   boolean order,
                                                   int page,
                                                   int size);
}
