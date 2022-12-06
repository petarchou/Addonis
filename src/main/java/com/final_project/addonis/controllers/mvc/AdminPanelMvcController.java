package com.final_project.addonis.controllers.mvc;

import com.final_project.addonis.models.Addon;
import com.final_project.addonis.models.Category;
import com.final_project.addonis.models.dtos.CategoryDto;
import com.final_project.addonis.services.contracts.AddonService;
import com.final_project.addonis.services.contracts.UserService;
import com.final_project.addonis.utils.exceptions.EntityNotFoundException;
import com.final_project.addonis.utils.exceptions.UnauthorizedOperationException;
import com.final_project.addonis.utils.helpers.CategoryHelper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin-panel")
public class AdminPanelMvcController {

    private final UserService userService;
    private final AddonService addonService;
    private final CategoryHelper categoryHelper;


    public AdminPanelMvcController(UserService userService, AddonService addonService, CategoryHelper categoryHelper) {
        this.userService = userService;
        this.addonService = addonService;
        this.categoryHelper = categoryHelper;
    }

    @ModelAttribute("pendingAddons")
    public List<Addon> populatePendingAddons() {
        return addonService.getAllPendingAddons();
    }

    // TODO restrict only for admins
    @GetMapping("/pending-addons")
    public String getPendingAddons() {
        return "adminPanel";
    }

    // TODO restrict only for admins
    @PostMapping("/approve/{id}")
    public String approveAddon(@PathVariable int id, CategoryDto category) {
        try {
            List<Category> categories = category.getCategories().stream()
                    .map(categoryHelper::fromCategoryName)
                    .collect(Collectors.toList());
            addonService.approveAddon(id, categories);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (UnauthorizedOperationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
        return "redirect:/admin-panel/pending-addons";
    }

    // TODO notify user if there is a problem with his addon and change state to draft
}
