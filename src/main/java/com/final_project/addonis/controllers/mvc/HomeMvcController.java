package com.final_project.addonis.controllers.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class HomeMvcController {

    @GetMapping("/")
    public String getHome() {
        return "dashboard";
    }

    @GetMapping("/login")
    public String getLogin() {
        return "login";
    }
}
