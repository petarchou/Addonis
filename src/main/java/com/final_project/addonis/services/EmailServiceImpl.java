package com.final_project.addonis.services;

import com.final_project.addonis.models.InvitedUser;
import com.final_project.addonis.models.PasswordResetToken;
import com.final_project.addonis.models.User;
import com.final_project.addonis.models.VerificationToken;
import com.final_project.addonis.repositories.contracts.InvitedUserRepository;
import com.final_project.addonis.repositories.contracts.UserRepository;
import com.final_project.addonis.services.contracts.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class EmailServiceImpl implements EmailService {


    public static final String SENDER_EMAIL = "addonis.donotreply@gmail.com";
    public static final String SENDER_NAME = "Addonis Marketplace";
    public static final String EMAIL_SEND_FAIL = "Failed to send email.";
    private final JavaMailSender mailSender;

    private final InvitedUserRepository invitedUserRepository;

    private final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final UserRepository userRepository;

    @Autowired
    public EmailServiceImpl(JavaMailSender emailSender, InvitedUserRepository invitedUserRepository, UserRepository userRepository) {
        this.mailSender = emailSender;
        this.invitedUserRepository = invitedUserRepository;
        this.userRepository = userRepository;
    }


    @Async
    @Override
    public void sendVerificationEmail(User user, String siteUrl, VerificationToken token) {
        try {
            String toEmail = user.getEmail();
            String subject = "Please verify your registration";
            String content = "Hello [[name]], <br>" +
                    "Please click the link below to verify your registration:<br>" +
                    "<h3><a href=\"[[URL]]\" target=\"_blank\">Confirm Registration</a></h3>" +
                    "Best regards,<br>" +
                    "The Addonis Team";

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom(SENDER_EMAIL, SENDER_NAME);
            helper.setTo(toEmail);
            helper.setSubject(subject);

            content = content.replace("[[name]]", user.getUsername());
            String verifyUrl = siteUrl + "/users/verify?code=" + token.getToken();

            content = content.replace("[[URL]]", verifyUrl);

            helper.setText(content, true);
            mailSender.send(message);
        }
        catch(MessagingException e) {
            throw new IllegalStateException(EMAIL_SEND_FAIL);
        }
        catch(UnsupportedEncodingException e) {
            logger.error("Invalid encoding for email sender name");
        }
    }

    @Override
    public void sendPasswordResetEmail(User user, String siteUrl, PasswordResetToken token) {
        try {
            String toEmail = user.getEmail();
            String subject = "Addonis Password Reset Requested";
            String content = "Hello [[name]], <br>" +
                    "Please click the link below and choose a new password for your account:<br>" +
                    "<h3><a href=\"[[URL]]\" target=\"_blank\">Reset Password</a></h3>" +
                    "If this wasn't you, be careful as somebody might be trying to steal your credential details.<br>"+
                    "Best regards,<br>" +
                    "The Addonis Team";

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom(SENDER_EMAIL, SENDER_NAME);
            helper.setTo(toEmail);
            helper.setSubject(subject);

            content = content.replace("[[name]]", user.getUsername());
            String verifyUrl = siteUrl + "/users/reset-password?code=" + token.getToken();

            content = content.replace("[[URL]]", verifyUrl);

            helper.setText(content, true);
            mailSender.send(message);
        }
        catch(MessagingException e) {
            throw new IllegalStateException(EMAIL_SEND_FAIL);
        }
        catch(UnsupportedEncodingException e) {
            logger.error("Invalid encoding for email sender name");
        }
    }


    @Override
    public void sendInvitationEmail(User referrer, String siteUrl, InvitedUser invitedUser) {
        Optional<InvitedUser> existingOptional = invitedUserRepository.findByEmail(invitedUser.getEmail());
        if (isAUser(invitedUser) || !wasInvitedMinFiveDaysAgo(existingOptional)) {
            throw new IllegalArgumentException("Invite was sent less than 5 days ago or user is already registered.");
        }
        if (existingOptional.isPresent()) {
            invitedUser = existingOptional.get();
        }
        invitedUser.setLastInviteDate(LocalDateTime.now());


        composeAndSendInvite(referrer, siteUrl, invitedUser);
        invitedUserRepository.saveAndFlush(invitedUser);
    }
    @Async
    public void composeAndSendInvite(User referrer, String siteUrl, InvitedUser invitedUser) {
        try {
            String subject = referrer.getUsername() + " invited you to join Addonis Marketplace!";
            String content = "Hey, there!<br>" +
                    "You were invited by [[referrer]] to join Addonis - the biggest marketplace for IDE addons!<br>" +
                    "Click the link below to make your registration:<br>" +
                    "<h3><a href=\"[[URL]]\" target=\"_blank\">Register</a></h3>" +
                    "Best regards,<br>" +
                    "The Addonis Team";

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
            helper.setTo(invitedUser.getEmail());
            helper.setSubject(subject);
            helper.setFrom(SENDER_EMAIL, SENDER_NAME);

            content = content.replace("[[referrer]]", referrer.getUsername());
            content = content.replace("[[URL]]", siteUrl + "/register");

            helper.setText(content, true);

            mailSender.send(message);
        }
        catch(MessagingException e) {
            throw new IllegalStateException(EMAIL_SEND_FAIL);
        }
        catch(UnsupportedEncodingException e) {
            logger.error("Invalid encoding for email sender name");
        }
    }


    private boolean isAUser(InvitedUser invitedUser) {
        return userRepository.findByEmail(invitedUser.getEmail()).isPresent();
    }

    private boolean wasInvitedMinFiveDaysAgo(Optional<InvitedUser> existing) {
        return existing.isEmpty() || !existing.get().getLastInviteDate().isAfter(LocalDateTime.now().minusDays(5));
    }
}
