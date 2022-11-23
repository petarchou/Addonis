package com.final_project.addonis.services.contracts;

import com.final_project.addonis.models.InvitedUser;
import com.final_project.addonis.models.User;
import com.final_project.addonis.models.VerificationToken;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;

public interface EmailService {


    void sendInvitationEmail(User referrer, String siteUrl, InvitedUser invitedUser) throws UnsupportedEncodingException;

    void sendVerificationEmail(User user, String siteUrl, VerificationToken token) throws UnsupportedEncodingException;

}
