package com.final_project.addonis.services;

import com.final_project.addonis.models.User;
import com.final_project.addonis.services.contracts.UserService;
import com.final_project.addonis.utils.exceptions.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MyUserDetailsServiceImplTests {
    @Mock
    private UserService userService;
    @InjectMocks
    private MyUserDetailsServiceImpl myUserDetailsService;

    @Test
    public void loadUserByUsername_should_returnUserDetails_when_UserWhitUsernameExist() {
        // Assert
        User mockUser = new User();
        when(userService.getByUsername(anyString())).thenReturn(mockUser);

        // Act, Assert
        assertInstanceOf(UserDetails.class, myUserDetailsService.loadUserByUsername("test"));

    }

    @Test
    public void loadUserByUsername_should_throwsException_when_UserWhitUsernameNotExist() {
        // Assert
        when(userService.getByUsername(anyString())).thenThrow(EntityNotFoundException.class);

        // Act, Assert
        assertThrows(UsernameNotFoundException.class,
                () -> myUserDetailsService.loadUserByUsername("test"));

    }
}
