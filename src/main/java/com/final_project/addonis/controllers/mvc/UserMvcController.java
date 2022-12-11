package com.final_project.addonis.controllers.mvc;

import com.final_project.addonis.models.Addon;
import com.final_project.addonis.models.User;
import com.final_project.addonis.models.dtos.CreateUserDto;
import com.final_project.addonis.models.dtos.PasswordDto;
import com.final_project.addonis.models.dtos.UserDto;
import com.final_project.addonis.services.contracts.AddonService;
import com.final_project.addonis.services.contracts.UserService;
import com.final_project.addonis.utils.exceptions.EntityNotFoundException;
import com.final_project.addonis.utils.exceptions.UnauthorizedOperationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/users")
public class UserMvcController {

    public static final String UNBLOCK_KEYWORD = "unblock";
    public static final String BLOCK_KEYWORD = "block";
    private final UserService userService;

    private final AddonService addonService;

    public UserMvcController(UserService userService, AddonService addonService) {
        this.userService = userService;
        this.addonService = addonService;
    }

    @GetMapping("/{id}/edit")
    public String editUser(@PathVariable int id, Model model) {
        User profile = userService.getById(id);
        model.addAttribute("passwordDto", new PasswordDto());
        return "edit_profile";
    }

    @GetMapping("/verify")
    public String verifyUser(@RequestParam("token") String tokenStr) {
        try {
            userService.verifyUser(tokenStr);
            return "verify_success";
        } catch(IllegalArgumentException e) {
            return "verify_fail";
        }
    }

    @GetMapping("/{id}")
    public String userProfileView(@PathVariable int id, Model model) {
        try {
            User userProfile = userService.getById(id);
            List<Addon> approvedAddons = addonService.getApprovedAddonsByUser(id);
            model.addAttribute("profile", userProfile);
            model.addAttribute("approvedAddons",approvedAddons);
            return "user_profile";
        } catch(EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,e.getMessage());
        }
    }

//    @Secured("ROLE_ADMIN")


    @PostMapping("/{id}/block-unblock")
    public String blockUnblock(@PathVariable int id) {
        try {
            User profile = userService.getById(id);
            String action = profile.isBlocked() ? UNBLOCK_KEYWORD : BLOCK_KEYWORD;
            userService.changeBlockedStatus(id,action);
            return "redirect:/users/" + id;
        } catch(UnauthorizedOperationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch(EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,e.getMessage());
        }
    }

    @ModelAttribute("isAuth")
    private boolean isAuthenticated(@CurrentSecurityContext SecurityContext context) {
        Authentication authentication = context.getAuthentication();
        boolean au = authentication != null && !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated();
        return au;
    }

    @ModelAttribute("loggedUser")
    private User getLoggedUser(Principal principal) {

        return principal == null ? null : userService.getByUsername(principal.getName());
    }
}
