package com.final_project.addonis.services;

import com.final_project.addonis.models.InvitedUser;
import com.final_project.addonis.models.Role;
import com.final_project.addonis.models.User;
import com.final_project.addonis.models.VerificationToken;
import com.final_project.addonis.repositories.contracts.InvitedUserRepository;
import com.final_project.addonis.repositories.contracts.RoleRepository;
import com.final_project.addonis.repositories.contracts.UserRepository;
import com.final_project.addonis.repositories.contracts.VerificationTokenRepository;
import com.final_project.addonis.services.contracts.EmailService;
import com.final_project.addonis.services.contracts.UserService;
import com.final_project.addonis.utils.exceptions.DuplicateEntityException;
import com.final_project.addonis.utils.exceptions.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    public static final String INVALID_ARGUMENT_ERR = "Invalid method argument : %s";
    private static final String INVALID_FIELDS = "You can search by filter,username or phoneNumber";
    private final UserRepository repository;
    private final EmailService emailService;

    private final RoleRepository roleRepository;

    private final VerificationTokenRepository verificationTokenRepository;

    private final InvitedUserRepository invitedUserRepository;


    public UserServiceImpl(UserRepository repository,
                           EmailService emailService,
                           RoleRepository roleRepository, VerificationTokenRepository verificationTokenRepository,
                           InvitedUserRepository invitedUserRepository) {
        this.repository = repository;
        this.emailService = emailService;
        this.roleRepository = roleRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.invitedUserRepository = invitedUserRepository;
    }

    @Override
    public List<User> getAll(Optional<String> keyword,
                             Optional<String> filterByField,
                             Optional<String> sortByField,
                             Optional<Boolean> order,
                             Optional<Integer> page,
                             Optional<Integer> size) {

        validateFields(filterByField);
        validateFields(sortByField);
        int pageOrDefault = page.orElse(0);
        int sizeOrDefault = size.orElse(10);
        String sortOrDefault = sortByField.orElse("id");
        boolean descOrder = order.orElse(true);

        return repository.findAllUsersByFilteringAndSorting(keyword,
                filterByField,
                sortOrDefault,
                descOrder,
                pageOrDefault,
                sizeOrDefault);
    }

    @Override
    public User getById(int id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", id));
    }

    @Override
    public User create(User user, String siteUrl) {
        verifyIsUniqueUsername(user);
        verifyIsUniqueEmail(user);
        verifyIsUniquePhone(user);
        user = repository.save(user);


        Optional<InvitedUser> invitedUser = invitedUserRepository.findByEmail(user.getEmail());
        invitedUser.ifPresent(invitedUserRepository::delete);

        VerificationToken token = createVerificationToken(user);
        emailService.sendVerificationEmail(user, siteUrl, token);
        return user;
    }

    @Override
    public User update(User user) {

        verifyIsUniqueEmail(user);
        verifyIsUniquePhone(user);

        return repository.saveAndFlush(user);
    }

    @Override
    public User delete(User user) {

        user.setDeleted(true);
        repository.saveAndFlush(user);

        return user;
    }

    @Override
    public void verifyUser(String tokenStr) {
        VerificationToken token = verificationTokenRepository.getByToken(tokenStr);

        if (token == null || token.getUser().isEnabled()) {
            throw new IllegalArgumentException("Verification failed - wrong token, expired token or user is verified");
        } else {
            User user = token.getUser();
            user.setVerified(true);
            verificationTokenRepository.delete(token);
            repository.saveAndFlush(user);
        }
    }

    @Override
    public User changeUserRole(int id, String roleName, String action) {
        User userToUpdate = getById(id);
        Role roleToChange = roleRepository.findByName(roleName.toUpperCase())
                .orElseThrow(() ->
                        new UnsupportedOperationException(
                                String.format(INVALID_ARGUMENT_ERR, "roleName")));

        switch (action.toLowerCase()) {
            case "promote":
                tryAddRole(userToUpdate, roleToChange);
                break;
            case "demote":
                tryRemoveRole(userToUpdate, roleToChange);
                break;
            default:
                throw new UnsupportedOperationException(String.format(INVALID_ARGUMENT_ERR, "action"));

        }

        return repository.saveAndFlush(userToUpdate);
    }

    @Override
    public User changeBlockedStatus(int id, String action) {
        User user = getById(id);
        switch (action.toLowerCase()) {
            case "block":
                tryBlockUser(user);
                break;
            case "unblock":
                tryUnblockUser(user);
                break;
            default:
                throw new UnsupportedOperationException("Invalid method argument: action");

        }

        return repository.saveAndFlush(user);
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


    private void tryAddRole(User userToUpdate, Role roleToChange) {
        if (userToUpdate.getRoles().contains(roleToChange)) {
            throw new DuplicateEntityException(String.format("User with id %d is already %s.",
                    userToUpdate.getId(),
                    roleToChange.getName().toLowerCase()));
        }
        userToUpdate.addRole(roleToChange);
    }

    private void tryRemoveRole(User userToUpdate, Role roleToChange) {
        if (!userToUpdate.getRoles().contains(roleToChange)) {
            throw new DuplicateEntityException(String.format("User with id %d is not %s",
                    userToUpdate.getId(),
                    roleToChange.getName().toLowerCase()));
        }
        userToUpdate.removeRole(roleToChange);
    }

    private void tryBlockUser(User user) {
        if (user.isBlocked())
            throw new DuplicateEntityException(
                    String.format("User with id %d is already blocked", user.getId()));
        user.setBlocked(true);
    }

    private void tryUnblockUser(User user) {
        if (!user.isBlocked())
            throw new DuplicateEntityException(
                    String.format("User with id %d is already unblocked", user.getId()));
        user.setBlocked(false);
    }

    private void verifyIsUniqueUsername(User user) {
        if (repository.existsUserByUsername(user.getUsername())) {
            User existingUser = getByUsername(user.getUsername());
            if (!user.equals(existingUser)) {
                throw new DuplicateEntityException("User", "username", user.getUsername());
            }
        }
    }

    private void verifyIsUniqueEmail(User user) {
        if (repository.existsUserByEmail(user.getEmail())) {
            User existingUser = getByEmail(user.getEmail());
            if (!user.equals(existingUser)) {
                throw new DuplicateEntityException("User", "username", user.getUsername());
            }
        }
    }

    private void verifyIsUniquePhone(User user) {
        if (repository.existsUserByPhoneNumber(user.getPhoneNumber())) {
            User existingUser = getByPhone(user.getPhoneNumber());
            if (!user.equals(existingUser)) {
                throw new DuplicateEntityException("User", "username", user.getUsername());
            }
        }
    }

    private VerificationToken createVerificationToken(User user) {
        VerificationToken token = new VerificationToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        return verificationTokenRepository.saveAndFlush(token);
    }

    private void validateFields(Optional<String> fields) {
        if (fields.isPresent()) {
            switch (fields.get()) {
                case "username":
                case "email":
                case "phoneNumber":
                    break;
                default:
                    throw new IllegalArgumentException(INVALID_FIELDS);
            }
        }
    }
}
