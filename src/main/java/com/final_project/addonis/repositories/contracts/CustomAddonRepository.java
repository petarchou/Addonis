package com.final_project.addonis.repositories.contracts;

import com.final_project.addonis.models.Addon;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface CustomAddonRepository {

    Page<Addon> findAllAddonsByFilteringAndSorting(Optional<String> keyword,
                                                   Optional<String> targetIde,
                                                   Optional<String> category,
                                                   boolean order,
                                                   int page,
                                                   int size);
}
