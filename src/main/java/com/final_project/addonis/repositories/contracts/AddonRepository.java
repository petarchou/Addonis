package com.final_project.addonis.repositories.contracts;

import com.final_project.addonis.models.Addon;
import com.final_project.addonis.models.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddonRepository extends JpaRepository<Addon, Integer>, CustomAddonRepository {

    @Query("select a from Addon a where a.id = :id and (a.state.name like 'approved' or a.state.name like 'pending')")
    Optional<Addon> findApprovedOrPendingAddonById(int id);
    @Query("select a from Addon a where a.id = :id and a.state.name like 'approved'")
    Optional<Addon> findAddonByIdAndStateNameApproved(int id);

    @Query("select a from Addon a where a.id = :id and a.state.name like 'pending'")
    Optional<Addon> findAddonByIdAndStateNamePending(int id);

    Optional<Addon> findByStateEqualsAndIdEquals(State state, int id);

    Optional<Addon> findByNameAndStateNameIgnoreCaseNot(String addonName, String stateName);

    @Query("select a from Addon a where a.name = :name and a.state.name like 'approved'")
    Optional<Addon> findByName(String name);

    @Query("select a from Addon a where a.state.name like 'pending'")
    List<Addon> getAllByStateNamePending();

    @Query("select a from Addon a where a.state.name like 'draft'")
    List<Addon> getAllByStateNameDraft();

    List<Addon> getAllByStateNameEqualsIgnoreCase(String name);

    @Query("select a from Addon a where a.state.name like 'approved' order by a.downloads desc")
    List<Addon> getAllByStateNameApprovedOrderByDownloads();

    @Query("select a from Addon a where a.state.name like 'approved' order by a.uploadedDate desc")
    List<Addon> getAllByStateNameApprovedOrderByUploadedDate();

    @Query("select a from Addon a where a.isFeatured = true")
    List<Addon> getAddonsByFeaturedTrue();

    @Query("select a from Addon a where a.creator.id = :userId and a.state.name like 'pending'")
    List<Addon> getPendingAddonsByUser(int userId);

    @Query("select a from Addon a where a.creator.id = :userId and a.state.name like 'approved'")
    List<Addon> getApprovedAddonsByUser(int userId);


    @Query("select a from Addon a where a.creator.id = :userId and a.state.name like 'draft'")
    List<Addon> getDraftedAddonsByUser(int userId);
}
