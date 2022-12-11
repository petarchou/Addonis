package com.final_project.addonis.services;

import com.final_project.addonis.models.InvitedUser;
import com.final_project.addonis.models.PasswordResetToken;
import com.final_project.addonis.models.User;
import com.final_project.addonis.models.VerificationToken;
import com.final_project.addonis.repositories.contracts.InvitedUserRepository;
import com.final_project.addonis.repositories.contracts.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceImplTests {
    @Mock
    private JavaMailSender mailSender;

    @Mock
    private InvitedUserRepository invitedUserRepository;

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    public void sendVerificationEmail_should_sendEmail_when_detailsIsValid() {
        // Arrange
        User user = new User();
        user.setUsername("testUsername");
        user.setEmail("testEmail");
        MimeMessage message = mock(MimeMessage.class);
        VerificationToken mockToken = new VerificationToken();
        mockToken.setToken("testToken");
        when(mailSender.createMimeMessage()).thenReturn(message);
        // Act
        emailService.sendVerificationEmail(user, "url", mockToken);

        //Assert
        verify(mailSender, times(1)).send(message);
    }

//    @Test
//    public void sendVerificationEmail_should_catchException_when_mimiHelperThrows() throws MessagingException, UnsupportedEncodingException {
//        // Arrange
//        User user = new User();
//        user.setUsername("testUsername");
//        user.setEmail("testEmail");
//        MimeMessage message = mock(MimeMessage.class);
//        VerificationToken mockToken = new VerificationToken();
//        mockToken.setToken("testToken");
//        MimeMessageHelper helper = mock(MimeMessageHelper.class);
//        when(mailSender.createMimeMessage()).thenReturn(message);
//        doThrow(MessagingException.class).when(helper).setFrom("test", "test");
//
//        // Act, Assert
//        assertThrows(IllegalArgumentException.class,
//                () -> emailService.sendVerificationEmail(user, "url", mockToken));
//    }

    @Test
    public void sendPasswordResetEmail_should_sendEmail_when_detailsIsValid() {
        User user = new User();
        user.setUsername("testUsername");
        user.setEmail("testEmail");
        MimeMessage message = mock(MimeMessage.class);
        PasswordResetToken mockToken = new PasswordResetToken();
        mockToken.setToken("testToken");
        when(mailSender.createMimeMessage()).thenReturn(message);

        // Act
        emailService.sendPasswordResetEmail(user, "url", mockToken);

        //Assert
        verify(mailSender, times(1)).send(message);
    }


    @Test
    public void sendInvitationEmail_should_sendEmail_when_detailsIsValid() {
        User user = new User();
        user.setUsername("testUsername");
        user.setEmail("testEmail");
        MimeMessage message = mock(MimeMessage.class);
        InvitedUser mockInvitedUser = new InvitedUser();
        mockInvitedUser.setEmail("testEmail");
        mockInvitedUser.setLastInviteDate(LocalDateTime.now().minusDays(6));
        when(mailSender.createMimeMessage()).thenReturn(message);
        when(invitedUserRepository.findByEmail(anyString())).thenReturn(Optional.of(mockInvitedUser));
        when(userRepository.findByEmailAndIsDeletedFalse(anyString())).thenReturn(Optional.empty());

        // Act
        emailService.sendInvitationEmail(user, "url", mockInvitedUser);

        //Assert
        verify(mailSender, times(1)).send(message);
    }

    @Test
    public void sendInvitationEmail_should_throwsException_when_lastInviteDateIsSmallerThanFiveDays() {
        InvitedUser mockInvitedUser = new InvitedUser();
        mockInvitedUser.setEmail("testEmail");
        mockInvitedUser.setLastInviteDate(LocalDateTime.now().minusDays(3));
        when(invitedUserRepository.findByEmail(anyString())).thenReturn(Optional.of(mockInvitedUser));

        // Act
        assertThrows(IllegalArgumentException.class,
                () -> emailService.sendInvitationEmail(new User(), "url", mockInvitedUser));
    }

    @Test
    public void sendInvitationEmail_should_throwsException_when_userIsAlreadyRegistered() {
        InvitedUser mockInvitedUser = new InvitedUser();
        mockInvitedUser.setEmail("testEmail");
        mockInvitedUser.setLastInviteDate(LocalDateTime.now().minusDays(3));
        when(userRepository.findByEmailAndIsDeletedFalse(anyString())).thenReturn(Optional.of(new User()));


        // Act
        assertThrows(IllegalArgumentException.class,
                () -> emailService.sendInvitationEmail(new User(), "url", mockInvitedUser));
    }


}
