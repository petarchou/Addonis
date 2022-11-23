package com.final_project.addonis.services;

import com.final_project.addonis.models.User;
import com.final_project.addonis.repositories.contracts.UserRepository;
import com.final_project.addonis.services.contracts.UserService;
import com.final_project.addonis.utils.exceptions.DuplicateEntityException;
import com.final_project.addonis.utils.exceptions.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository repository;


    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
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
    public User create(User user) {

        verifyIsUniqueUsername(user.getUsername());
        verifyIsUniqueEmail(user.getEmail());
        verifyIsUniquePhone(user.getPhoneNumber());

        return repository.save(user);

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
}
