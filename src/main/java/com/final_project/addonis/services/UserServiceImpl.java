package com.final_project.addonis.services;

import com.final_project.addonis.models.User;
import com.final_project.addonis.repositories.contracts.UserRepository;
import com.final_project.addonis.services.contracts.UserService;
import com.final_project.addonis.utils.config.springsecurity.PasswordEncoder;
import com.final_project.addonis.utils.exceptions.DuplicateEntityException;
import com.final_project.addonis.utils.exceptions.EntityNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder.getBCryptPasswordEncoder();
    }

    @Override
    public List<User> getAll() {
        return repository.findAllByDeletedFalse();
    }

    @Override
    public User getById(int id) {
        Optional<User> user = repository.findById(id);

        if (user.isPresent()) {
            return user.get();
        }

        throw new EntityNotFoundException("User", id);
    }

    @Override
    public User create(User user) {
        verifyIsUniqueUsername(user.getUsername());
        verifyIsUniqueEmail(user.getEmail());
        verifyIsUniquePhone(user.getPhoneNumber());
        //in mapper ?
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return repository.save(user);
    }

    @Override
    public User update(User user) {
        verifyIsUniqueEmail(user.getEmail());
        verifyIsUniquePhone(user.getPhoneNumber());
        //put this in mapper?
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return repository.save(user);
    }

    @Override
    public User delete(User user) {
        user.setDeleted(true);
        repository.save(user);
        return user;
    }

    @Override
    public User getByUsername(String username) {
        Optional<User> user = repository.findByUsername(username);
        if(user.isEmpty()) {
            throw new EntityNotFoundException("User","username",username);
        }
        return  user.get();
    }

    private void verifyIsUniqueUsername(String username) {
        if (repository.existsByUsername(username)) {
            throw new DuplicateEntityException("User", "username", username);
        }
    }

    private void verifyIsUniqueEmail(String email) {
        if (repository.existsByEmail(email)) {
            throw new DuplicateEntityException("User", "email", email);
        }
    }

    private void verifyIsUniquePhone(String phone) {
        if (repository.existsByPhoneNumber(phone)) {
            throw new DuplicateEntityException("User", "phone", phone);
        }
    }
}
