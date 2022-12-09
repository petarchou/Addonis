package com.final_project.addonis.services.contracts;

import com.final_project.addonis.models.*;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;

public interface EmailService {

    void sendEmailForRejectedAddon(User toUser, Addon addon);

    void sendInvitationEmail(User referrer, String siteUrl, InvitedUser invitedUser);

    void sendVerificationEmail(User user, String siteUrl, VerificationToken token);

    void sendPasswordResetEmail(User user, String siteUrl, PasswordResetToken token);
}
