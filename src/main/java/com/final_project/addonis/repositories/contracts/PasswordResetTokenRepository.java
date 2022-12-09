package com.final_project.addonis.repositories.contracts;

import com.final_project.addonis.models.PasswordResetToken;
import com.final_project.addonis.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {

    Optional<PasswordResetToken> findByUser(User user);
    Optional<PasswordResetToken> findByToken(String tokenStr);
}
