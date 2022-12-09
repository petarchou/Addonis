package com.final_project.addonis.controllers.mvc;

import com.final_project.addonis.models.Addon;
import com.final_project.addonis.models.Category;
import com.final_project.addonis.models.User;
import com.final_project.addonis.models.dtos.UsersFilter;
import com.final_project.addonis.models.dtos.UsersFilterDto;
import com.final_project.addonis.services.contracts.AddonService;
import com.final_project.addonis.services.contracts.CategoryService;
import com.final_project.addonis.services.contracts.UserService;
import com.final_project.addonis.utils.exceptions.EntityNotFoundException;
import com.final_project.addonis.utils.exceptions.UnauthorizedOperationException;
import com.final_project.addonis.utils.helpers.CategoryHelper;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    @GetMapping("/users")
    public String getUsers(Model model,
                           @ModelAttribute UsersFilter usersFilter) {

        Page<User> users = userService.getAll(usersFilter.getSearch(),
                usersFilter.getFilterByField(),
                usersFilter.getSortByField(),
                usersFilter.getOrder(),
                usersFilter.getPage(),
                usersFilter.getSize());
        model.addAttribute("usersPage", users);
        model.addAttribute("userFilter",new UsersFilterDto());

        int totalPages = users.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("userPages", pageNumbers);
        }
        return "all_users";
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
        return "pending_addon";
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
