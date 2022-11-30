package com.final_project.addonis.services;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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


    @Test
    public void getAll_should_callFindAllUsersByFilteringAndSorting_when_parametersIsValid() {
        // Arrange
        List<User> repoUserList = List.of(new User(),new User());
        when(mockUserRepository.findAllUsersByFilteringAndSorting(any(),
                any(),
                anyString(),
                anyBoolean(),
                anyInt(),
                anyInt()))
                .thenReturn(repoUserList);

        // Act
        List<User> serviceUsersList = userService.getAll(Optional.empty(),
                Optional.of("username"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());

        // Assert
        assertSame(serviceUsersList,repoUserList);
    }
    @Test
    public void getAll_should_throwsException_when_fieldsForFilteringOrSortingIsInvalid() {
        // Arrange, Act, Assert
        assertThrows(IllegalArgumentException.class,() -> // Act
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
        when(mockUserRepository.findById(anyInt())).thenReturn(Optional.of(mockUser));

        //Act, Assert
        assertEquals(userService.getById(1).getId(), 1);
        verify(mockUserRepository, times(1))
                .findById(anyInt());

    }

    @Test
    public void getById_should_throwsException_when_userNotExist() {
        // Arrange
        when(mockUserRepository.findById(anyInt())).thenReturn(Optional.empty());

        //Act, Assert
        assertThrows(EntityNotFoundException.class, () -> userService.getById(anyInt()));
        verify(mockUserRepository, times(1))
                .findById(anyInt());

    }

    @Test
    public void create_should_createUser_when_userInfoIsValid() {
        // Arrange
        User mockUser = new User();
        mockUser.setUsername("test");
        mockUser.setEmail("test");
        mockUser.setPhoneNumber("test");
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
        User mockUser1 = new User();
        mockUser1.setUsername("test");
        mockUser1.setId(1);
        User mockUser2 = new User();
        mockUser2.setUsername("test");
        mockUser2.setId(2);
        when(mockUserRepository.findByUsername(any())).thenReturn(Optional.of(mockUser2));
        when(mockUserRepository.existsUserByUsername(any())).thenReturn(true);

        //Act,Assert
        assertThrows(DuplicateEntityException.class,
                () -> userService.create(mockUser1, ""));
    }

    @Test
    public void create_should_throwsException_when_userEmailExist() {
        // Arrange
        User mockUser1 = new User();
        mockUser1.setEmail("existingEmail");
        mockUser1.setId(1);
        User mockUser2 = new User();
        mockUser2.setEmail("existingEmail");
        mockUser2.setId(2);
        when(mockUserRepository.findByEmail(any())).thenReturn(Optional.of(mockUser2));
        when(mockUserRepository.existsUserByEmail(any())).thenReturn(true);

        //Act,Assert
        assertThrows(DuplicateEntityException.class,
                () -> userService.create(mockUser1, ""));
    }

    @Test
    public void create_should_throwsException_when_userPhoneExist() {
        // Arrange
        User mockUser1 = new User();
        mockUser1.setPhoneNumber("0898888888");
        mockUser1.setId(1);
        User mockUser2 = new User();
        mockUser2.setPhoneNumber("0898888888");
        mockUser2.setId(2);
        when(mockUserRepository.findByPhoneNumber(any())).thenReturn(Optional.of(mockUser2));
        when(mockUserRepository.existsUserByPhoneNumber(any())).thenReturn(true);

        // Act,Assert
        assertThrows(DuplicateEntityException.class,
                () -> userService.create(mockUser1, ""));
    }

    @Test
    public void update_should_updateUser_when_userInfoIsValid() {
        // Arrange
        User mockUser = new User();
        mockUser.setUsername("test");
        mockUser.setEmail("test");
        mockUser.setPhoneNumber("test");
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
        User mockUser = new User();
        mockUser.setDeleted(false);
        when(mockUserRepository.saveAndFlush(any())).thenReturn(null);

        // Act
        mockUser = userService.delete(mockUser);

        // Assert
        assertTrue(mockUser.isDeleted());
    }

    @Test
    public void verifyUser_should_setIsVerifiedToTrue_when_tokenExist() {
        //Arrange
        VerificationToken mockToken = new VerificationToken();
        User mockUser = new User();
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
        User mockUser = new User();
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
        User mockUser = new User();
        mockUser.setBlocked(false);
        when(mockUserRepository.saveAndFlush(any())).thenReturn(mockUser);
        when(mockUserRepository.findById(any())).thenReturn(Optional.of(mockUser));

        // Act
        mockUser = userService.changeBlockedStatus(anyInt(), "block");

        // Assert
        assertTrue(mockUser.isBlocked());
    }

    @Test
    public void changeBlockStatus_should_throwsException_when_userIsBlockedAndActionIsBlock() {
        // Arrange
        User mockUser = new User();
        mockUser.setBlocked(true);
        when(mockUserRepository.findById(any())).thenReturn(Optional.of(mockUser));

        // Act, Assert
        assertThrows(DuplicateEntityException.class,
                () -> userService.changeBlockedStatus(anyInt(), "block"));
    }

    @Test
    public void changeBlockStatus_should_unblockUser_when_userIsBlocked() {
        // Arrange
        User mockUser = new User();
        mockUser.setBlocked(true);
        when(mockUserRepository.saveAndFlush(any())).thenReturn(mockUser);
        when(mockUserRepository.findById(any())).thenReturn(Optional.of(mockUser));

        // Act
        mockUser = userService.changeBlockedStatus(anyInt(), "unblock");

        // Assert
        assertFalse(mockUser.isBlocked());
    }

    @Test
    public void changeBlockStatus_should_throwsException_when_userIsUnblockedAndActionIsUnblock() {
        // Arrange
        User mockUser = new User();
        mockUser.setBlocked(false);
        when(mockUserRepository.findById(any())).thenReturn(Optional.of(mockUser));

        // Act, Assert
        assertThrows(DuplicateEntityException.class,
                () -> userService.changeBlockedStatus(anyInt(), "unblock"));
    }

    @Test
    public void changeBlockStatus_should_throwsException_when_actionIsInvalid() {
        // Arrange
        User mockUser = new User();
        when(mockUserRepository.findById(any())).thenReturn(Optional.of(mockUser));

        // Act, Assert
        assertThrows(UnsupportedOperationException.class,
                () -> userService.changeBlockedStatus(anyInt(), "test"));
    }

    @Test
    public void changeUserRole_should_addRoleToSetOfRoles_when_userNotContainTheRole() {
        // Arrange
        User mockUser = new User();
        mockUser.setRoles(new HashSet<>());
        Role mockRole = new Role();
        mockRole.setName("testRole");
        when(mockUserRepository.findById(any())).thenReturn(Optional.of(mockUser));
        when(mockRoleRepository.findByName(any())).thenReturn(Optional.of(mockRole));
        when(mockUserRepository.saveAndFlush(any())).thenReturn(mockUser);

        // Act
        mockUser = userService.changeUserRole(anyInt(), "testRole", "promote");

        // Assert
        assertTrue(mockUser.getRoles().contains(mockRole));
    }

    @Test
    public void changeUserRole_should_throwsException_when_userContainTheRoleAndTryPromote() {
        // Arrange
        User mockUser = new User();
        mockUser.setRoles(new HashSet<>());
        Role mockRole = new Role();
        mockRole.setName("testRole");
        mockUser.getRoles().add(mockRole);
        when(mockUserRepository.findById(any())).thenReturn(Optional.of(mockUser));
        when(mockRoleRepository.findByName(any())).thenReturn(Optional.of(mockRole));

        // Act, Assert
        assertThrows(DuplicateEntityException.class,
                () -> userService.changeUserRole(anyInt(), "testRole", "promote"));
    }

    @Test
    public void changeUserRole_should_removeRoleFromSetOfRoles_when_userContainTheRole() {
        // Arrange
        User mockUser = new User();
        mockUser.setRoles(new HashSet<>());
        Role mockRole = new Role();
        mockRole.setName("testRole");
        mockUser.getRoles().add(mockRole);
        when(mockUserRepository.findById(any())).thenReturn(Optional.of(mockUser));
        when(mockRoleRepository.findByName(any())).thenReturn(Optional.of(mockRole));
        when(mockUserRepository.saveAndFlush(any())).thenReturn(mockUser);

        // Act
        mockUser = userService.changeUserRole(anyInt(), "testRole", "demote");

        // Assert
        assertFalse(mockUser.getRoles().contains(mockRole));
    }

    @Test
    public void changeUserRole_should_throwsException_when_userNotContainTheRoleAndTryToDemote() {
        // Arrange
        User mockUser = new User();
        mockUser.setRoles(new HashSet<>());
        Role mockRole = new Role();
        mockRole.setName("testRole");
        when(mockUserRepository.findById(any())).thenReturn(Optional.of(mockUser));
        when(mockRoleRepository.findByName(any())).thenReturn(Optional.of(mockRole));

        // Act, Assert
        assertThrows(DuplicateEntityException.class,
                () -> userService.changeUserRole(anyInt(), "testRole", "demote"));
    }

    @Test
    public void changeUserRole_should_throwsException_when_roleIsInvalid() {
        // Arrange
        User mockUser = new User();
        when(mockUserRepository.findById(anyInt())).thenReturn(Optional.of(mockUser));
        when(mockRoleRepository.findByName(any())).thenReturn(Optional.empty());

        // Act, Arrange
        assertThrows(UnsupportedOperationException.class,
                () -> userService.changeUserRole(mockUser.getId(), "testRole", anyString()));

    }

    @Test
    public void changeUserRole_should_throwsException_when_actionIsInvalid() {
        // Arrange
        User mockUser = new User();
        Role mockRole = new Role();
        when(mockUserRepository.findById(anyInt())).thenReturn(Optional.of(mockUser));
        when(mockRoleRepository.findByName(any())).thenReturn(Optional.of(mockRole));

        // Act, Arrange
        assertThrows(UnsupportedOperationException.class,
                () -> userService.changeUserRole(mockUser.getId(), "testRole", "testAction"));

    }

}
