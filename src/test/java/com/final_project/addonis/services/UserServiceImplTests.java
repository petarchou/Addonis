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
import com.final_project.addonis.utils.exceptions.DuplicateEntityException;
import com.final_project.addonis.utils.exceptions.EntityNotFoundException;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserServiceImplTests {
    @Mock
    private UserRepository mockUserRepository;
    @Mock
    private EmailService mockEmailService;
    @Mock
    private RoleRepository mockRoleRepository;
    @Mock
    private VerificationTokenRepository mockVerificationTokenRepository;
    @Mock
    private InvitedUserRepository mockInvitedUserRepository;
    @InjectMocks
    private UserServiceImpl userService;
    private User mockUser = new User();
    private final User mockUser2 = new User();

    @BeforeEach
    public void createMockUser() {
        mockUser.setId(0);
        mockUser.setUsername("testUsername");
        mockUser.setEmail("test@mail.com");
        mockUser.setPassword("testPassword");
        mockUser.setPhoneNumber("0885216546");
        mockUser.setPhotoUrl("testUrl");
        mockUser.setRoles(new HashSet<>());
        mockUser.setVerified(true);
        mockUser.setBlocked(false);
        mockUser.setDeleted(false);

        mockUser.setId(1);
        mockUser.setUsername("testUsername2");
        mockUser.setEmail("test2@mail.com");
        mockUser.setPassword("testPassword2");
        mockUser.setPhoneNumber("0885216547");
        mockUser.setPhotoUrl("testUrl2");
        mockUser.setRoles(new HashSet<>());
        mockUser.setVerified(true);
        mockUser.setBlocked(false);
        mockUser.setDeleted(false);
    }

    @Test
    public void getAll_should_callFindAllUsersByFilteringAndSorting_when_parametersIsValid() {
        // Arrange
        Page<User> repoUserList = new PageImpl<>(List.of(mockUser,mockUser2));
        when(mockUserRepository.findAllUsersByFilteringAndSorting(any(),
                any(),
                anyString(),
                anyBoolean(),
                anyInt(),
                anyInt()))
                .thenReturn(repoUserList);

        // Act
        Page<User> serviceUsersList = userService.getAll(Optional.empty(),
                Optional.of("username"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());

        // Assert
        assertSame(serviceUsersList, repoUserList);
    }

    @Test
    public void getAll_should_throwsException_when_fieldsForFilteringOrSortingIsInvalid() {
        // Arrange, Act, Assert
        assertThrows(IllegalArgumentException.class, () -> // Act
                userService.getAll(Optional.empty(),
                        Optional.of("testField"),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));
    }

    @Test
    public void getById_should_returnUser_when_userExist() {
        // Arrange
        User mockUser = new User();
        mockUser.setId(1);
        when(mockUserRepository.findByIdAndIsDeletedFalse(anyInt())).thenReturn(Optional.of(mockUser));

        //Act, Assert
        assertEquals(userService.getById(1).getId(), 1);
        verify(mockUserRepository, times(1))
                .findByIdAndIsDeletedFalse(anyInt());

    }

    @Test
    public void getById_should_throwsException_when_userNotExist() {
        // Arrange
        when(mockUserRepository.findByIdAndIsDeletedFalse(anyInt())).thenReturn(Optional.empty());

        //Act, Assert
        assertThrows(EntityNotFoundException.class, () -> userService.getById(anyInt()));
        verify(mockUserRepository, times(1))
                .findByIdAndIsDeletedFalse(anyInt());

    }

    @Test
    public void create_should_createUser_when_userInfoIsValid() {
        // Arrange
        when(mockUserRepository.save(any())).thenReturn(mockUser);
        when(mockInvitedUserRepository.findByEmail(anyString())).thenReturn(Optional.empty());


        //Act
        userService.create(mockUser, "");

        //Assert
        verify(mockUserRepository, times(1)).save(any());
    }

    @Test
    public void create_should_throwsException_when_userUsernameExist() {
        // Arrange
        when(mockUserRepository.findByUsernameAndIsDeletedFalse(any())).thenReturn(Optional.of(mockUser2));
        when(mockUserRepository.existsUserByUsernameAndIsDeletedFalse(any())).thenReturn(true);

        //Act,Assert
        assertThrows(DuplicateEntityException.class,
                () -> userService.create(mockUser, ""));
    }

    @Test
    public void create_should_throwsException_when_userEmailExist() {
        // Arrange
        mockUser.setEmail("existingEmail");
        mockUser.setId(1);
        mockUser2.setEmail("existingEmail");
        mockUser2.setId(2);
        when(mockUserRepository.findByEmailAndIsDeletedFalse(any())).thenReturn(Optional.of(mockUser2));
        when(mockUserRepository.existsUserByEmailAndIsDeletedFalse(any())).thenReturn(true);

        //Act,Assert
        assertThrows(DuplicateEntityException.class,
                () -> userService.create(mockUser, ""));
    }

    @Test
    public void create_should_throwsException_when_userPhoneExist() {
        // Arrange
        mockUser.setPhoneNumber("0898888888");
        mockUser.setId(1);
        mockUser2.setPhoneNumber("0898888888");
        mockUser2.setId(2);
        when(mockUserRepository.findByPhoneNumberAndIsDeletedFalse(any())).thenReturn(Optional.of(mockUser2));
        when(mockUserRepository.existsUserByPhoneNumberAndIsDeletedFalse(any())).thenReturn(true);

        // Act,Assert
        assertThrows(DuplicateEntityException.class,
                () -> userService.create(mockUser, ""));
    }

    @Test
    public void update_should_updateUser_when_userInfoIsValid() {
        // Arrange
        when(mockUserRepository.saveAndFlush(any())).thenReturn(null);

        // Act
        userService.update(mockUser);

        // Assert
        verify(mockUserRepository, times(1))
                .saveAndFlush(any());
    }

    @Test
    public void delete_should_setIsDeletedToTrue() {
        // Arrange
        mockUser.setDeleted(false);
        User clone = mockUser.toBuilder().isDeleted(true).build();
        when(mockUserRepository.saveAndFlush(any())).thenReturn(clone);

        // Act
        mockUser = userService.delete(mockUser);

        // Assert
        assertTrue(mockUser.isDeleted());
    }

    @Test
    public void verifyUser_should_setIsVerifiedToTrue_when_tokenExist() {
        //Arrange
        VerificationToken mockToken = new VerificationToken();
        mockUser.setVerified(false);
        mockToken.setToken(RandomString.make(64));
        mockToken.setUser(mockUser);
        when(mockVerificationTokenRepository.getByToken(any())).thenReturn(mockToken);

        //Act
        userService.verifyUser(any());

        //Assert
        verify(mockVerificationTokenRepository, times(1))
                .delete(any());
        verify(mockUserRepository, times(1))
                .saveAndFlush(any());

    }

    @Test
    public void verifyUser_should_throwsException_when_tokenIsInvalid() {
        //Arrange
        when(mockVerificationTokenRepository.getByToken(any())).thenReturn(null);

        //Act, Assert
        assertThrows(IllegalArgumentException.class,
                () -> userService.verifyUser(any()));

    }

    @Test
    public void verifyUser_should_throwsException_when_userIsEnable() {
        //Arrange
        VerificationToken mockToken = new VerificationToken();
        mockToken.setToken(RandomString.make(64));
        mockUser.setVerified(true);
        mockToken.setUser(mockUser);
        when(mockVerificationTokenRepository.getByToken(any())).thenReturn(mockToken);

        //Act, Assert
        assertThrows(IllegalArgumentException.class,
                () -> userService.verifyUser(any()));

    }

    @Test
    public void changeBlockStatus_should_blockUser_when_userIsUnblocked() {
        // Arrange
        mockUser.setBlocked(false);
        User clone = mockUser.toBuilder().build();
        clone.setBlocked(true);
        when(mockUserRepository.saveAndFlush(any())).thenReturn(clone);
        when(mockUserRepository.findByIdAndIsDeletedFalse(anyInt())).thenReturn(Optional.of(mockUser));

        // Act
        mockUser = userService.changeBlockedStatus(anyInt(), "block");

        // Assert
        assertTrue(mockUser.isBlocked());
    }

    @Test
    public void changeBlockStatus_should_throwsException_when_userIsBlockedAndActionIsBlock() {
        // Arrange
        mockUser.setBlocked(true);
        when(mockUserRepository.findByIdAndIsDeletedFalse(anyInt())).thenReturn(Optional.of(mockUser));

        // Act, Assert
        assertThrows(DuplicateEntityException.class,
                () -> userService.changeBlockedStatus(anyInt(), "block"));
    }

    @Test
    public void changeBlockStatus_should_unblockUser_when_userIsBlocked() {
        // Arrange
        User clone = mockUser.toBuilder().build();
        mockUser.setBlocked(true);
        when(mockUserRepository.saveAndFlush(any())).thenReturn(clone);
        when(mockUserRepository.findByIdAndIsDeletedFalse(anyInt())).thenReturn(Optional.of(mockUser));

        // Act
        mockUser = userService.changeBlockedStatus(anyInt(), "unblock");

        // Assert
        assertFalse(mockUser.isBlocked());
    }

    @Test
    public void changeBlockStatus_should_throwsException_when_userIsUnblockedAndActionIsUnblock() {
        // Arrange
        mockUser.setBlocked(false);
        when(mockUserRepository.findByIdAndIsDeletedFalse(anyInt())).thenReturn(Optional.of(mockUser));

        // Act, Assert
        assertThrows(DuplicateEntityException.class,
                () -> userService.changeBlockedStatus(anyInt(), "unblock"));
    }

    @Test
    public void changeBlockStatus_should_throwsException_when_actionIsInvalid() {
        // Arrange
        when(mockUserRepository.findByIdAndIsDeletedFalse(anyInt())).thenReturn(Optional.of(mockUser));

        // Act, Assert
        assertThrows(UnsupportedOperationException.class,
                () -> userService.changeBlockedStatus(anyInt(), "test"));
    }

    @Test
    public void changeUserRole_should_addRoleToSetOfRoles_when_userNotContainTheRole() {
        // Arrange
        Role mockRole = new Role();
        mockRole.setName("testRole");
        User clone = mockUser.toBuilder().roles(new HashSet<>(mockUser.getRoles())).build();
        clone.addRole(mockRole);
        when(mockUserRepository.findByIdAndIsDeletedFalse(anyInt())).thenReturn(Optional.of(mockUser));
        when(mockRoleRepository.findByName(any())).thenReturn(Optional.of(mockRole));
        when(mockUserRepository.saveAndFlush(any())).thenReturn(clone);

        // Act
        mockUser = userService.changeUserRole(anyInt(), "testRole", "promote");

        // Assert
        assertTrue(mockUser.getRoles().contains(mockRole));
    }

    @Test
    public void changeUserRole_should_throwsException_when_userContainTheRoleAndTryPromote() {
        // Arrange
        Role mockRole = new Role();
        mockRole.setName("testRole");
        mockUser.getRoles().add(mockRole);
        when(mockUserRepository.findByIdAndIsDeletedFalse(anyInt())).thenReturn(Optional.of(mockUser));
        when(mockRoleRepository.findByName(any())).thenReturn(Optional.of(mockRole));

        // Act, Assert
        assertThrows(DuplicateEntityException.class,
                () -> userService.changeUserRole(anyInt(), "testRole", "promote"));
    }

    @Test
    public void changeUserRole_should_removeRoleFromSetOfRoles_when_userContainTheRole() {
        // Arrange
        Role mockRole = new Role();
        mockRole.setName("testRole");
        User clone = mockUser.toBuilder().roles(new HashSet<>(mockUser.getRoles())).build();
        mockUser.getRoles().add(mockRole);
        when(mockUserRepository.findByIdAndIsDeletedFalse(anyInt())).thenReturn(Optional.of(mockUser));
        when(mockRoleRepository.findByName(any())).thenReturn(Optional.of(mockRole));
        when(mockUserRepository.saveAndFlush(any())).thenReturn(clone);

        // Act
        mockUser = userService.changeUserRole(anyInt(), "testRole", "demote");

        // Assert
        assertFalse(mockUser.getRoles().contains(mockRole));
    }

    @Test
    public void changeUserRole_should_throwsException_when_userNotContainTheRoleAndTryToDemote() {
        // Arrange
        Role mockRole = new Role();
        mockRole.setName("testRole");
        when(mockUserRepository.findByIdAndIsDeletedFalse(anyInt())).thenReturn(Optional.of(mockUser));
        when(mockRoleRepository.findByName(any())).thenReturn(Optional.of(mockRole));

        // Act, Assert
        assertThrows(DuplicateEntityException.class,
                () -> userService.changeUserRole(anyInt(), "testRole", "demote"));
    }

    @Test
    public void changeUserRole_should_throwsException_when_roleIsInvalid() {
        // Arrange
        when(mockUserRepository.findByIdAndIsDeletedFalse(anyInt())).thenReturn(Optional.of(mockUser));
        when(mockRoleRepository.findByName(any())).thenReturn(Optional.empty());

        // Act, Arrange
        assertThrows(IllegalArgumentException.class,
                () -> userService.changeUserRole(mockUser.getId(), "testRole", anyString()));

    }

    @Test
    public void changeUserRole_should_throwsException_when_actionIsInvalid() {
        // Arrange
        Role mockRole = new Role();
        when(mockUserRepository.findByIdAndIsDeletedFalse(anyInt())).thenReturn(Optional.of(mockUser));
        when(mockRoleRepository.findByName(any())).thenReturn(Optional.of(mockRole));

        // Act, Arrange
        assertThrows(UnsupportedOperationException.class,
                () -> userService.changeUserRole(mockUser.getId(), "testRole", "testAction"));

    }

    @Test
    public void deleteExpiredInvitations_should_deleteInvitedUser_when_invitationIsExpired() {
        //Arrange
        InvitedUser mockUser1 = new InvitedUser();
        mockUser1.setId(1);
        mockUser1.setLastInviteDate(LocalDateTime.now().minusDays(5));
        InvitedUser mockUser2 = new InvitedUser();
        mockUser2.setId(2);
        mockUser2.setLastInviteDate(LocalDateTime.now().minusDays(5));
        List<InvitedUser> invitedUsers = List.of(mockUser1, mockUser2);
        when(mockInvitedUserRepository.findAll()).thenReturn(invitedUsers);
        doNothing().when(mockInvitedUserRepository).delete(any());

        // Act
        userService.deleteExpiredInvitations();
        // Assert
        verify(mockInvitedUserRepository, times(2)).delete(any());

    }
}
