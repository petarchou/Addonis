package com.final_project.addonis.repositories.contracts;

import com.final_project.addonis.models.State;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StateRepository extends JpaRepository<State, Integer> {

    State findByName(String name);
}
