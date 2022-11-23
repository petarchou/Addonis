package com.final_project.addonis.repositories.contracts;

import com.final_project.addonis.models.InvitedUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvitedUserRepository extends JpaRepository<InvitedUser,Integer> {

    Optional<InvitedUser> findByEmail(String email);

}
