package com.final_project.addonis.controllers.mvc;

import com.final_project.addonis.models.Addon;
import com.final_project.addonis.models.InvitedUser;
import com.final_project.addonis.models.User;
import com.final_project.addonis.models.dtos.EmailDto;
import com.final_project.addonis.models.dtos.PasswordDto;
import com.final_project.addonis.services.contracts.AddonService;
import com.final_project.addonis.services.contracts.EmailService;
import com.final_project.addonis.services.contracts.UserService;
import com.final_project.addonis.utils.exceptions.EntityNotFoundException;
import com.final_project.addonis.utils.exceptions.PasswordNotMatchException;
import com.final_project.addonis.utils.exceptions.UnauthorizedOperationException;
import com.final_project.addonis.utils.helpers.EmailHelper;
import com.final_project.addonis.utils.mappers.InvitedUserMapper;
import com.final_project.addonis.utils.mappers.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/users")
public class UserMvcController {

    public static final String UNBLOCK_KEYWORD = "unblock";
    public static final String BLOCK_KEYWORD = "block";
    private final UserService userService;
    private final AddonService addonService;
    private final UserMapper mapper;
    private final InvitedUserMapper invitedUserMapper;
    private final EmailService emailService;
    private final EmailHelper emailHelper;

    public UserMvcController(UserService userService,
                             AddonService addonService,
                             UserMapper mapper,
                             InvitedUserMapper invitedUserMapper,
                             EmailService emailService,
                             EmailHelper emailHelper) {
        this.userService = userService;
        this.addonService = addonService;
        this.mapper = mapper;
        this.invitedUserMapper = invitedUserMapper;
        this.emailService = emailService;
        this.emailHelper = emailHelper;
    }

    @GetMapping("/{id}/edit")
    public String editUser(@PathVariable int id, Model model) {
        User profile = userService.getById(id);
        model.addAttribute("passwordDto", new PasswordDto());
        return "edit_profile";
    }

    @GetMapping("/verify")
    public String verifyUser(@RequestParam("code") String tokenStr) {
        try {
            userService.verifyUser(tokenStr);
            return "verify_success";
        } catch (IllegalArgumentException e) {
            return "verify_fail";
        }
    }

    @GetMapping("/{id}")
    public String userProfileView(@PathVariable int id, Model model) {
        try {
            User userProfile = userService.getById(id);
            List<Addon> approvedAddons = addonService.getApprovedAddonsByUser(id);
            model.addAttribute("profile", userProfile);
            model.addAttribute("approvedAddons", approvedAddons);
            return "user_profile";
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

//    @Secured("ROLE_ADMIN")


    @PostMapping("/{id}/block-unblock")
    public String blockUnblock(@PathVariable int id) {
        try {
            User profile = userService.getById(id);
            String action = profile.isBlocked() ? UNBLOCK_KEYWORD : BLOCK_KEYWORD;
            userService.changeBlockedStatus(id, action);
            return "redirect:/users/" + id;
        } catch (UnauthorizedOperationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @ModelAttribute("isAuth")
    private boolean isAuthenticated(@CurrentSecurityContext SecurityContext context) {
        Authentication authentication = context.getAuthentication();
        return authentication != null
                && !(authentication instanceof AnonymousAuthenticationToken)
                && authentication.isAuthenticated();
    }

    @GetMapping("/{id}/change-password")
    public String changePasswordView(Model model,
                                     @PathVariable int id) {
        model.addAttribute("password", new PasswordDto());
        return "change_password";
    }

    @PostMapping("/{id}/change-password")
    public String changePassword(@PathVariable int id,
                                 @Valid @ModelAttribute PasswordDto passwordDto,
                                 BindingResult bindingResult,
                                 Model model) {
        try {
            User user = userService.getById(id);
            mapper.fromPasswordDto(user, passwordDto);
            userService.update(user);
        } catch (PasswordNotMatchException e) {
            bindingResult.rejectValue("confirmNewPassword", "password_error",
                    "Confirm password did not match.");
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("password", new PasswordDto());
            return "change_password";
        }
        return "redirect:/";
    }

    @GetMapping("/{id}/invite")
    public String inviteFriendView(Model model,
                                   @PathVariable int id) {
        model.addAttribute("email", new EmailDto());
        return "invite_friend";
    }

    @PostMapping("/{id}/invite")
    public String inviteFriend(@Valid @ModelAttribute(name = "email") EmailDto toEmail
            , @PathVariable int id, HttpServletRequest request) {
        try {
            User user = userService.getById(id);
            InvitedUser invitedUser = invitedUserMapper.fromEmail(toEmail.getEmail());
            emailService.sendInvitationEmail(user, emailHelper.getSiteUrl(request), invitedUser);
            return "redirect:/";
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }


    @ModelAttribute("loggedUser")
    private User getLoggedUser(Principal principal) {
        return principal == null
                ? null
                : userService.getByUsername(principal.getName());
    }
}
