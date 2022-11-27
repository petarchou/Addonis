package com.final_project.addonis.repositories.contracts;

import com.final_project.addonis.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    boolean existsByName(String name);
    Optional<Category> findByName(String name);
}
