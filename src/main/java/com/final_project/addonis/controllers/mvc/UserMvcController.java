package com.final_project.addonis.controllers.mvc;

import com.final_project.addonis.services.contracts.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/users")
public class UserMvcController {

    private final UserService service;

    public UserMvcController(UserService service) {
        this.service = service;
    }

    @GetMapping("/verify")
    public String verifyUser(@RequestParam("token") String tokenStr) {
        try {
            service.verifyUser(tokenStr);
            return "verify_success";
        }
        catch(IllegalArgumentException e) {
            return "verify_fail";
        }
    }
}
