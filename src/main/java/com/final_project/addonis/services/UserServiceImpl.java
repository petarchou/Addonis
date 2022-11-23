package com.final_project.addonis.services;

import com.final_project.addonis.models.InvitedUser;
import com.final_project.addonis.models.User;
import com.final_project.addonis.models.VerificationToken;
import com.final_project.addonis.repositories.contracts.InvitedUserRepository;
import com.final_project.addonis.repositories.contracts.UserRepository;
import com.final_project.addonis.repositories.contracts.VerificationTokenRepository;
import com.final_project.addonis.services.contracts.EmailService;
import com.final_project.addonis.services.contracts.UserService;
import com.final_project.addonis.utils.config.springsecurity.PasswordEncoder;
import com.final_project.addonis.utils.exceptions.DuplicateEntityException;
import com.final_project.addonis.utils.exceptions.EntityNotFoundException;
import net.bytebuddy.utility.RandomString;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private final EmailService emailService;

    private final VerificationTokenRepository tokenRepository;

    private final InvitedUserRepository invitedUserRepository;


    public UserServiceImpl(UserRepository repository,
                           EmailService emailService,
                           VerificationTokenRepository tokenRepository,
                           InvitedUserRepository invitedUserRepository) {
        this.repository = repository;
        this.emailService = emailService;
        this.tokenRepository = tokenRepository;
        this.invitedUserRepository = invitedUserRepository;
    }

    @Override
    public List<User> getAll() {

        return repository.getAllByDeletedFalse();
    }

    @Override
    public User getById(int id) {

        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", id));
    }

    @Override
    public User create(User user, String siteUrl) throws UnsupportedEncodingException {
        verifyIsUniqueUsername(user.getUsername());
        verifyIsUniqueEmail(user.getEmail());
        verifyIsUniquePhone(user.getPhoneNumber());
        user = repository.save(user);


        Optional<InvitedUser> invitedUser = invitedUserRepository.findByEmail(user.getEmail());
        invitedUser.ifPresent(invitedUserRepository::delete);

        VerificationToken token = createVerificationToken(user);
        emailService.sendVerificationEmail(user, siteUrl, token);
        return user;
    }

    @Override
    public User update(User user) {

        verifyIsUniqueUsername(user.getUsername());
        verifyIsUniqueEmail(user.getEmail());
        verifyIsUniquePhone(user.getPhoneNumber());

        return repository.saveAndFlush(user);
    }

    @Override
    public User delete(User user) {

        user.setDeleted(true);
        repository.saveAndFlush(user);

        return user;
    }

    @Override
    public User getByUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User", "username", username));
    }

    @Override
    public User getByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User", "email", email));
    }

    @Override
    public User getByPhone(String phone) {
        return repository.findByPhoneNumber(phone)
                .orElseThrow(() -> new EntityNotFoundException("User", "phone", phone));
    }

    @Override
    public void verifyUser(String tokenStr) {
        VerificationToken token = tokenRepository.getByToken(tokenStr);

        if (token == null || token.getUser().isEnabled()) {
            throw new IllegalArgumentException("Verification failed - wrong token or user is verified");
        } else {
            User user = token.getUser();
            user.setVerified(true);
            tokenRepository.delete(token);
            repository.saveAndFlush(user);
        }
    }


    private void verifyIsUniqueEmail(String email) {
        boolean exist = true;
        User user = new User();

        try {
            user = getByEmail(email);
        } catch (EntityNotFoundException e) {
            exist = false;
        }

        if (exist && !email.equals(user.getEmail())) {
            throw new DuplicateEntityException("User", "email", email);
        }
    }

    private void verifyIsUniquePhone(String phone) {
        boolean exist = true;
        User user = new User();

        try {
            user = getByPhone(phone);
        } catch (EntityNotFoundException e) {
            exist = false;
        }

        if (exist && !phone.equals(user.getPhoneNumber())) {
            throw new DuplicateEntityException("User", "phone", phone);
        }
    }

    private void verifyIsUniqueUsername(String username) {
        boolean exist = true;
        User user = new User();

        try {
            user = getByUsername(username);
        } catch (EntityNotFoundException e) {
            exist = false;
        }

        if (exist && !username.equals(user.getUsername())) {
            throw new DuplicateEntityException("User", "username", username);
        }
    }

    private VerificationToken createVerificationToken(User user) {
        VerificationToken token = new VerificationToken();
        token.setToken(RandomString.make(64));
        token.setUser(user);
        return tokenRepository.saveAndFlush(token);
    }
}
