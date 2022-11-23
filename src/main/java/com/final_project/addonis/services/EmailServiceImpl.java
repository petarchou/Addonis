package com.final_project.addonis.services;

import com.final_project.addonis.models.InvitedUser;
import com.final_project.addonis.models.User;
import com.final_project.addonis.models.VerificationToken;
import com.final_project.addonis.repositories.contracts.InvitedUserRepository;
import com.final_project.addonis.repositories.contracts.UserRepository;
import com.final_project.addonis.services.contracts.EmailService;
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
    private final JavaMailSender mailSender;

    private final InvitedUserRepository invitedUserRepository;

    private final UserRepository userRepository;

    @Autowired
    public EmailServiceImpl(JavaMailSender emailSender, InvitedUserRepository invitedUserRepository, UserRepository userRepository) {
        this.mailSender = emailSender;
        this.invitedUserRepository = invitedUserRepository;
        this.userRepository = userRepository;
    }


    @Async
    @Override
    public void sendVerificationEmail(User user, String siteUrl, VerificationToken token) throws UnsupportedEncodingException {
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
            throw new IllegalStateException("Failed to send email.");
        }
    }


    @Override
    public void sendInvitationEmail(User referrer, String siteUrl, InvitedUser invitedUser) throws UnsupportedEncodingException {
        Optional<InvitedUser> existingOptional = invitedUserRepository.findByEmail(invitedUser.getEmail());
        if (isAUser(invitedUser) || !wasInvitedMinFiveDaysAgo(existingOptional)) {
            throw new IllegalArgumentException("Invite was sent less than 5 days ago or user is already registered.");
        }
        invitedUser = updateDateIfExists(invitedUser, existingOptional);


        composeAndSendInvite(referrer, siteUrl, invitedUser);
        invitedUserRepository.saveAndFlush(invitedUser);
    }
    @Async
    public void composeAndSendInvite(User referrer, String siteUrl, InvitedUser invitedUser) throws UnsupportedEncodingException {
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
            throw new IllegalStateException("Failed to send email.");
        }
    }

    private InvitedUser updateDateIfExists(InvitedUser invitedUser, Optional<InvitedUser> optionalUser) {
        if (optionalUser.isPresent()) {
            invitedUser = optionalUser.get();
            invitedUser.setLastInviteDate(LocalDateTime.now());
        }

        return invitedUser;
    }


    private boolean isAUser(InvitedUser invitedUser) {
        return userRepository.findByEmail(invitedUser.getEmail()).isPresent();
    }

    private boolean wasInvitedMinFiveDaysAgo(Optional<InvitedUser> existing) {
        return existing.isEmpty() || !existing.get().getLastInviteDate().isAfter(LocalDateTime.now().minusDays(5));
    }
}
