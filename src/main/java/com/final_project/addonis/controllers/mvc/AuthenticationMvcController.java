package com.final_project.addonis.controllers.mvc;

import com.final_project.addonis.models.dtos.LoginDto;
import com.final_project.addonis.services.contracts.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthenticationMvcController {

    private final UserService userService;


    @Autowired
    public AuthenticationMvcController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    private String showLoginPage(Model model) {
        model.addAttribute("login", new LoginDto());
        return "login";
    }

}
