package com.final_project.addonis.repositories.contracts;

import com.final_project.addonis.models.Addon;
import com.final_project.addonis.models.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddonRepository extends JpaRepository<Addon, Integer> {
    @Query("select a from Addon a where a.id = :id and a.state.name like 'approved'")
    Optional<Addon> findById(int id);
    @Query("select a from Addon a where a.name = :name and a.state.name like 'approved'")
    Optional<Addon> findByName(String name);
    @Query("select a from Addon a where a.state.name like 'approved'")
    List<Addon> getAllByStateNameApproved();

    @Query("select a from Addon a where a.state.name like 'pending'")
    List<Addon> getAllByStateNamePending();
}
