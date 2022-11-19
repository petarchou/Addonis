package com.final_project.addonis.services;

import com.final_project.addonis.models.User;
import com.final_project.addonis.repositories.contracts.UserRepository;
import com.final_project.addonis.services.contracts.UserService;
import com.final_project.addonis.utils.exceptions.DuplicateEntityException;
import com.final_project.addonis.utils.exceptions.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
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
        checkUsername(user.getUsername());
        checkEmail(user.getEmail());
        checkPhone(user.getPhoneNumber());
        return repository.save(user);
    }

    @Override
    public User update(User user) {
        checkEmail(user.getEmail());
        checkPhone(user.getPhoneNumber());
        return repository.save(user);
    }

    @Override
    public User delete(User user) {
        user.setDeleted(true);
        repository.save(user);
        return user;
    }

    private void checkUsername(String username) {
        if (repository.existsByUsername(username)) {
            throw new DuplicateEntityException("User", "username", username);
        }
    }

    private void checkEmail(String email) {
        if (repository.existsByEmail(email)) {
            throw new DuplicateEntityException("User", "email", email);
        }
    }

    private void checkPhone(String phone) {
        if (repository.existsByPhoneNumber(phone)) {
            throw new DuplicateEntityException("User", "phone", phone);
        }
    }
}
