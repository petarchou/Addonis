package com.final_project.addonis.repositories.contracts;

import com.final_project.addonis.models.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer>{

    boolean existsByName(String name);

    Optional<Tag> findByName(String name);

}
