package com.final_project.addonis.repositories.contracts;

import com.final_project.addonis.models.Addon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddonRepository extends JpaRepository<Addon, Integer>, CustomAddonRepository {
    @Query("select a from Addon a where a.id = :id and a.state.name like 'approved'")
    Optional<Addon> findAddonByIdAndStateNameApproved(int id);

    @Query("select a from Addon a where a.id = :id and a.state.name like 'pending'")
    Optional<Addon> findAddonByIdAndStateNamePending(int id);

    @Query("select a from Addon a where a.name = :name and a.state.name like 'approved'")
    Optional<Addon> findByName(String name);

    @Query("select a from Addon a where a.state.name like 'pending'")
    List<Addon> getAllByStateNamePending();

    List<Addon> getAllByStateNameEqualsIgnoreCase(String name);

    @Query("select a from Addon a where a.state.name like 'approved' order by a.downloads desc")
    List<Addon> getAllByStateNameApprovedOrderByDownloads();

    @Query("select a from Addon a where a.state.name like 'approved' order by a.uploadedDate desc")
    List<Addon> getAllByStateNameApprovedOrderByUploadedDate();

    @Query("select a from Addon a where a.isFeatured = true")
    List<Addon> getAddonsByFeaturedTrue();


    boolean existsByName(String name);
}
