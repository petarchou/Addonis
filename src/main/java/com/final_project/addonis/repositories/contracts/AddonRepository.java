package com.final_project.addonis.repositories.contracts;

import com.final_project.addonis.models.Addon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface AddonRepository extends JpaRepository<Addon, Integer> {
    Optional<Addon> findByName(String name);
}
