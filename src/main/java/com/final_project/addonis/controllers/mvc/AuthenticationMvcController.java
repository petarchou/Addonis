package com.final_project.addonis.controllers.mvc;

import com.final_project.addonis.models.PasswordResetToken;
import com.final_project.addonis.models.User;
import com.final_project.addonis.models.dtos.CreateUserDto;
import com.final_project.addonis.models.dtos.EmailDto;
import com.final_project.addonis.models.dtos.ForgottenPasswordDto;
import com.final_project.addonis.models.dtos.LoginDto;
import com.final_project.addonis.services.contracts.PasswordResetTokenService;
import com.final_project.addonis.services.contracts.UserService;
import com.final_project.addonis.utils.exceptions.*;
import com.final_project.addonis.utils.mappers.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Controller
public class AuthenticationMvcController {

    private final UserService userService;
    private final PasswordResetTokenService passwordResetTokenService;

    private final UserMapper userMapper;


    @Autowired
    public AuthenticationMvcController(UserService userService,
                                       PasswordResetTokenService passwordResetTokenService,
                                       UserMapper userMapper) {
        this.userService = userService;
        this.passwordResetTokenService = passwordResetTokenService;
        this.userMapper = userMapper;
    }


    @GetMapping("/login")
    public String showLoginView(Model model) {
        model.addAttribute("login", new LoginDto());
        return "login";
    }


    @GetMapping("/register")
    public String showRegisterView(Model model  ) {
        model.addAttribute("registerDto", new CreateUserDto());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute(name = "registerDto") CreateUserDto registerDto,
                           BindingResult bindingResult,
                           HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
        try {
            User user = userMapper.fromCreateDto(registerDto);
                userService.create(user, getSiteUrl(request));
        }catch (PasswordNotMatchException e) {
            bindingResult.rejectValue("confirmPassword", "password_error",
                    "Confirm password did not match.");
        }
        catch (DuplicateUsernameException e) {
            bindingResult.rejectValue("username","duplicate_username",
                    e.getMessage());
        } catch (DuplicateEmailException e) {
            bindingResult.rejectValue("email","duplicate_email",
                    e.getMessage());
        } catch (DuplicatePhoneException e) {
                bindingResult.rejectValue("phoneNumber","duplicate_phone",
                    e.getMessage());
        }
        if (bindingResult.hasErrors()) {
            return "register";
        }
        return "register_success";
    }

    @GetMapping("/forgotten-password")
    public String showForgottenPasswordView(Model model) {
        EmailDto emailDto = new EmailDto();
        model.addAttribute("emailDto", emailDto);
        return "forgotten_password";
    }

    @PostMapping("/forgotten-password")
    public String sendResetPasswordEmail(@Valid @ModelAttribute("emailDto") EmailDto emailDto,
                                         BindingResult bindingResult,
                                         HttpServletRequest request,
                                         Model model
                                         ) {

        if(bindingResult.hasErrors()) {
            return "forgotten_password";
        }

        try {
            User user = userService.getByEmail(emailDto.getEmail());
            userService.sendResetPasswordRequest(user, getSiteUrl(request));
            EmailDto newEmailDto = new EmailDto();
            newEmailDto.setSent(true);
            model.addAttribute("emailDto", newEmailDto);
        } catch (EntityNotFoundException e) {
            bindingResult.rejectValue("email","invalid_email",
                    "We couldn't find an account associated with this email.");
        }
        return "forgotten_password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordView(@RequestParam(name = "token") String tokenStr, Model model) {
        try {
            passwordResetTokenService.validateToken(tokenStr);
            model.addAttribute("passwordDto", new ForgottenPasswordDto());
            model.addAttribute("token", tokenStr);
            return "create_password";
        } catch (EntityNotFoundException | ExpiredTokenException e) {
            return "invalid_password_token";
        }
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam(name = "token") String tokenStr,
                                @Valid @ModelAttribute(name = "passwordDto") ForgottenPasswordDto passwordDto,
                                BindingResult bindingResult, Model model) {
        if(bindingResult.hasErrors()) {
            model.addAttribute("token", tokenStr);
            return "create_password";
        }


        try {
            PasswordResetToken token = passwordResetTokenService.getByToken(tokenStr);
            User user = token.getUser();
            user = userMapper.setNewPassword(user, passwordDto);
            userService.update(user);
            passwordResetTokenService.delete(token);
            return "redirect:/";
        }catch (PasswordNotMatchException e) {
            bindingResult.rejectValue("confirmNewPassword", "password_error",
                    "Confirm password did not match.");
            model.addAttribute("token", tokenStr);
            return "create_password";
        }
        catch (EntityNotFoundException e) {
            return "invalid_password_token";
        }
    }


    private String getSiteUrl(HttpServletRequest request) {
        String siteUrl = request.getRequestURL().toString();
        return siteUrl.replace(request.getServletPath(), "");
    }

    @ModelAttribute("isAuth")
    private boolean isAuthenticated(@CurrentSecurityContext SecurityContext context) {
        Authentication authentication = context.getAuthentication();
        return authentication != null
                && !(authentication instanceof AnonymousAuthenticationToken)
                && authentication.isAuthenticated();
    }

}
