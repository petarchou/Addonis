package com.final_project.addonis.controllers.mvc;

import com.final_project.addonis.models.Addon;
import com.final_project.addonis.services.contracts.AddonService;
import com.final_project.addonis.utils.exceptions.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/addons")
public class AddonMvcController {

    private final AddonService addonService;

    public AddonMvcController(AddonService addonService) {
        this.addonService = addonService;
    }

    @GetMapping("/{id}")
    public String getAddon(@PathVariable int id, Model model) {
        try {
            Addon addon = addonService.getAddonById(id);
            model.addAttribute("addon", addon);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
        return "addonView";
    }
}
