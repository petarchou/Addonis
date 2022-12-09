package com.final_project.addonis.services;

import com.final_project.addonis.models.PasswordResetToken;
import com.final_project.addonis.repositories.contracts.PasswordResetTokenRepository;
import com.final_project.addonis.services.contracts.PasswordResetTokenService;
import com.final_project.addonis.utils.exceptions.EntityNotFoundException;
import com.final_project.addonis.utils.exceptions.ExpiredTokenException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {

    private final PasswordResetTokenRepository passwordTokenRepository;

    public PasswordResetTokenServiceImpl(PasswordResetTokenRepository passwordTokenRepository) {
        this.passwordTokenRepository = passwordTokenRepository;
    }

    @Override
    public PasswordResetToken getByToken(String tokenStr) {
        return passwordTokenRepository.findByToken(tokenStr)
                .orElseThrow(()->new EntityNotFoundException(String.format("Invalid Token %s", tokenStr)));
    }

    @Override
    public void validateToken(String tokenStr) {
        PasswordResetToken token = getByToken(tokenStr);
        if(token.isExpired()) {
            throw new ExpiredTokenException("Your token is expired, please request a new one.");
        }
    }

    @Override
    public void delete(PasswordResetToken token) {
        passwordTokenRepository.delete(token);
    }

    @Async
    @Scheduled(fixedRate = 15,timeUnit = TimeUnit.MINUTES)
    public void deleteExpiredTokens() {
        List<PasswordResetToken> tokens = passwordTokenRepository.findAll();
        tokens.forEach(token -> {
            if(token.isExpired()) {
                delete(token);
            }
        });
    }
}
