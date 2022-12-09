package com.final_project.addonis.controllers.mvc;

import com.final_project.addonis.models.Addon;
import com.final_project.addonis.models.Category;
import com.final_project.addonis.models.User;
import com.final_project.addonis.services.contracts.AddonService;
import com.final_project.addonis.services.contracts.CategoryService;
import com.final_project.addonis.services.contracts.UserService;
import com.final_project.addonis.utils.exceptions.EntityNotFoundException;
import com.final_project.addonis.utils.exceptions.UnauthorizedOperationException;
import com.final_project.addonis.utils.helpers.CategoryHelper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    private final CategoryService categoryService;


    public AdminPanelMvcController(UserService userService, AddonService addonService, CategoryHelper categoryHelper, CategoryService categoryService) {
        this.userService = userService;
        this.addonService = addonService;
        this.categoryHelper = categoryHelper;
        this.categoryService = categoryService;
    }

    @ModelAttribute("allCategories")
    public List<Category> populateCategories() {
        return categoryService.getAll();
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

    // TODO restrict only for admins and creators
    @GetMapping("/pending/{id}")
    public String getPendingAddon(@PathVariable int id, Model model) {
        try {
            Addon addon = addonService.getPendingAddonById(id);
            model.addAttribute("pendingAddon", addon);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (UnauthorizedOperationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
        return "pendingAddonView";
    }

    // TODO restrict only for admins
    @PostMapping("/approve/{id}")
    public String approveAddon(@PathVariable int id,
                               @RequestParam("categoriesIn") List<String> categoriesIn) {
        try {
            Addon addon = addonService.getPendingAddonById(id);
            List<Category> categories = categoriesIn.stream()
                    .map(categoryHelper::fromCategoryName)
                    .collect(Collectors.toList());
            addonService.approveAddon(addon.getId(), categories);
            return "redirect:/admin-panel/pending-addons";
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (UnauthorizedOperationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    // TODO restrict only for admins
    @PostMapping("/notify/{id}")
    public String notifyUser(@PathVariable int id) {
        try {
            Addon currentAddon = addonService.getPendingAddonById(id);
            User creator = currentAddon.getCreator();
            addonService.notifyUser(creator, currentAddon);
            return "redirect:/admin-panel/pending-addons";
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (UnauthorizedOperationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }
}
