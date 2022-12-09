package com.final_project.addonis.services.contracts;

import com.final_project.addonis.models.PasswordResetToken;

public interface PasswordResetTokenService {

    PasswordResetToken getByToken(String tokenStr);

    void validateToken(String tokenStr);

    void delete(PasswordResetToken token);

    void deleteExpiredTokens();
}
