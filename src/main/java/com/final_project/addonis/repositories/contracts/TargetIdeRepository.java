package com.final_project.addonis.repositories.contracts;

import com.final_project.addonis.models.TargetIde;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface TargetIdeRepository extends JpaRepository<TargetIde, Integer> {
    boolean existsByName(String name);
    Optional<TargetIde> findByNameIgnoreCase(String name);
}
