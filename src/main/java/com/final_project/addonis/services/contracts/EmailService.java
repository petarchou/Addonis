package com.final_project.addonis.services.contracts;

import com.final_project.addonis.models.InvitedUser;
import com.final_project.addonis.models.PasswordResetToken;
import com.final_project.addonis.models.User;
import com.final_project.addonis.models.VerificationToken;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;

public interface EmailService {

    void sendEmailForRejectedAddon(String toUser, String email);

    void sendInvitationEmail(User referrer, String siteUrl, InvitedUser invitedUser);

    void sendVerificationEmail(User user, String siteUrl, VerificationToken token);

    void sendPasswordResetEmail(User user, String siteUrl, PasswordResetToken token);
}
