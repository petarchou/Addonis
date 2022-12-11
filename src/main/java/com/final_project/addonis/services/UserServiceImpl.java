package com.final_project.addonis.services;

import com.final_project.addonis.models.*;
import com.final_project.addonis.repositories.contracts.*;
import com.final_project.addonis.services.contracts.EmailService;
import com.final_project.addonis.services.contracts.UserService;
import com.final_project.addonis.utils.exceptions.*;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {
    public static final String INVALID_ARGUMENT_ERR = "Invalid method argument : %s";
    private static final String INVALID_FIELDS = "You can search by filter,username or phoneNumber";
    private final UserRepository repository;
    private final EmailService emailService;

    private final RoleRepository roleRepository;

    private final VerificationTokenRepository verificationTokenRepository;

    private final InvitedUserRepository invitedUserRepository;

    private final PasswordResetTokenRepository passwordTokenRepository;


    public UserServiceImpl(UserRepository repository,
                           EmailService emailService,
                           RoleRepository roleRepository, VerificationTokenRepository verificationTokenRepository,
                           InvitedUserRepository invitedUserRepository,
                           PasswordResetTokenRepository passwordTokenRepository) {
        this.repository = repository;
        this.emailService = emailService;
        this.roleRepository = roleRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.invitedUserRepository = invitedUserRepository;
        this.passwordTokenRepository = passwordTokenRepository;
    }

    @Override
    public Page<User> getAll(Optional<String> keyword,
                             Optional<String> filterByField,
                             Optional<String> sortByField,
                             Optional<Boolean> order,
                             Optional<Integer> page,
                             Optional<Integer> size) {

        filterByField = validateFields(filterByField);
        sortByField = validateFields(sortByField);
        int pageOrDefault = page.orElse(1);
        int sizeOrDefault = size.orElse(10);
        String sortOrDefault = sortByField.orElse("username");
        boolean descOrder = order.orElse(true);

        return repository.findAllUsersByFilteringAndSorting(
                keyword,
                filterByField,
                sortOrDefault,
                descOrder,
                pageOrDefault,
                sizeOrDefault);
    }

    @Override
    public User getById(int id) {
        return repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("User", id));
    }

    @Override
    public User create(User user, String siteUrl) {
        verifyIsUniqueUsername(user);
        verifyIsUniqueEmail(user);
        verifyIsUniquePhone(user);
        User clone = repository.save(user);


        Optional<InvitedUser> invitedUser = invitedUserRepository.findByEmail(clone.getEmail());
        invitedUser.ifPresent(invitedUserRepository::delete);

        VerificationToken token = createVerificationToken(clone);
        emailService.sendVerificationEmail(clone, siteUrl, token);
        return clone;
    }

    @Override
    public User update(User user) {

        verifyIsUniqueEmail(user);
        verifyIsUniquePhone(user);

        return repository.saveAndFlush(user);
    }

    @Override
    public User delete(User user) {
        User clone = user.toBuilder().isDeleted(true).build();
        return repository.saveAndFlush(clone);
    }

    @Override
    public void sendResetPasswordRequest(User user, String siteUrl) {
        Optional<PasswordResetToken> existingToken = passwordTokenRepository.findByUser(user);
        existingToken.ifPresent(passwordTokenRepository::delete);
        PasswordResetToken token = createPasswordResetToken(user);
        emailService.sendPasswordResetEmail(user, siteUrl, token);
    }

    @Override
    public void verifyUser(String tokenStr) {
        VerificationToken token = verificationTokenRepository.getByToken(tokenStr);

        if (token == null || token.getUser().isEnabled()) {
            throw new IllegalArgumentException("Verification failed - wrong token or user is verified");
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
                        new IllegalArgumentException(
                                String.format(INVALID_ARGUMENT_ERR, "roleName")));

        switch (action.toLowerCase()) {
            case "promote":
                userToUpdate = tryAddRole(userToUpdate, roleToChange);
                break;
            case "demote":
                userToUpdate = tryRemoveRole(userToUpdate, roleToChange);
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
                user = tryBlockUser(user);
                break;
            case "unblock":
                user = tryUnblockUser(user);
                break;
            default:
                throw new UnsupportedOperationException("Invalid method argument: action");

        }
        return repository.saveAndFlush(user);
    }

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.DAYS)
    public void deleteExpiredInvitations() {
        invitedUserRepository.findAll().forEach(invitedUser -> {
            if (invitedUser.getLastInviteDate().isBefore(LocalDateTime.now())) {
                invitedUserRepository.delete(invitedUser);
            }
        });
    }


    @Override
    public User getByUsername(String username) {
        return repository.findByUsernameAndIsDeletedFalse(username)
                .orElseThrow(() -> new EntityNotFoundException("User", "username", username));
    }

    @Override
    public User getByEmail(String email) {
        return repository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new EntityNotFoundException("User", "email", email));
    }

    @Override
    public User getByPhone(String phone) {
        return repository.findByPhoneNumberAndIsDeletedFalse(phone)
                .orElseThrow(() -> new EntityNotFoundException("User", "phone", phone));
    }


    private User tryAddRole(User userToUpdate, Role roleToChange) {
        User clone = userToUpdate.toBuilder()
                .roles(new HashSet<>(userToUpdate.getRoles())).build();
        if (clone.getRoles().contains(roleToChange)) {
            throw new DuplicateEntityException(String.format("User with id %d is already %s.",
                    userToUpdate.getId(),
                    roleToChange.getName().toLowerCase()));
        }
        clone.addRole(roleToChange);
        return clone;
    }

    private User tryRemoveRole(User userToUpdate, Role roleToChange) {
        User clone = userToUpdate.toBuilder()
                .roles(new HashSet<>(userToUpdate.getRoles())).build();
        if (!clone.getRoles().contains(roleToChange)) {
            throw new DuplicateEntityException(String.format("User with id %d is not %s",
                    userToUpdate.getId(),
                    roleToChange.getName().toLowerCase()));
        }
        clone.removeRole(roleToChange);
        return clone;
    }

    private User tryBlockUser(User user) {
        User clone = user.toBuilder().build();
        if (clone.isBlocked())
            throw new DuplicateEntityException(
                    String.format("User with id %d is already blocked", user.getId()));
        clone.setBlocked(true);
        return clone;
    }

    private User tryUnblockUser(User user) {
        User clone = user.toBuilder().build();
        if (!clone.isBlocked())
            throw new DuplicateEntityException(
                    String.format("User with id %d is already unblocked", user.getId()));
        clone.setBlocked(false);
        return clone;
    }

    private void verifyIsUniqueUsername(User user) {
        if (repository.existsUserByUsernameAndIsDeletedFalse(user.getUsername())) {
            User existingUser = getByUsername(user.getUsername());
            if (!user.equals(existingUser)) {
                throw new DuplicateUsernameException("User", "username", user.getUsername());
            }
        }
    }

    private void verifyIsUniqueEmail(User user) {
        if (repository.existsUserByEmailAndIsDeletedFalse(user.getEmail())) {
            User existingUser = getByEmail(user.getEmail());
            if (!user.equals(existingUser)) {
                throw new DuplicateEmailException("User", "email", user.getEmail());
            }
        }
    }

    private void verifyIsUniquePhone(User user) {
        if (repository.existsUserByPhoneNumberAndIsDeletedFalse(user.getPhoneNumber())) {
            User existingUser = getByPhone(user.getPhoneNumber());
            if (!user.equals(existingUser)) {
                throw new DuplicatePhoneException("User", "phone", user.getPhoneNumber());
            }
        }
    }

    private VerificationToken createVerificationToken(User user) {
        VerificationToken token = new VerificationToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        return verificationTokenRepository.saveAndFlush(token);
    }

    private PasswordResetToken createPasswordResetToken(User user) {
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpirationDate(LocalDateTime.now().plusMinutes(15));
        return passwordTokenRepository.saveAndFlush(token);
    }

    private Optional<String> validateFields(Optional<String> fields) {

        if (fields.isPresent()) {
            switch (fields.get()) {
                case "username":
                case "email":
                case "phoneNumber":
                    break;
                case "":
                    return Optional.empty();
                default:
                    throw new IllegalArgumentException(INVALID_FIELDS);
            }
        }
        return fields;
    }
}
