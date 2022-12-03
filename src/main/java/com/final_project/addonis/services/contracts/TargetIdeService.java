package com.final_project.addonis.services.contracts;

import com.final_project.addonis.models.TargetIde;

import java.util.List;

public interface TargetIdeService {
    List<TargetIde> getAll();

    TargetIde getById(int id);

    TargetIde create(TargetIde targetIde);

    TargetIde update(TargetIde targetIde);

    void delete(TargetIde targetIde);

    boolean existByName(String name);
}
