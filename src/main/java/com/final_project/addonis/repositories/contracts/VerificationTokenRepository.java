package com.final_project.addonis.repositories.contracts;

import com.final_project.addonis.models.User;
import com.final_project.addonis.models.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Integer> {

    VerificationToken getByToken(String tokenStr);

    Optional<VerificationToken> findByUser(User user);
}
