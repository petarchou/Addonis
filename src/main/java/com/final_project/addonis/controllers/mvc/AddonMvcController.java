package com.final_project.addonis.controllers.mvc;

import com.final_project.addonis.models.*;
import com.final_project.addonis.models.dtos.*;
import com.final_project.addonis.services.contracts.*;
import com.final_project.addonis.utils.exceptions.DuplicateEntityException;
import com.final_project.addonis.utils.exceptions.EntityNotFoundException;
import com.final_project.addonis.utils.exceptions.GithubApiException;
import com.final_project.addonis.utils.exceptions.UnauthorizedOperationException;
import com.final_project.addonis.utils.mappers.AddonMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/addons")
public class AddonMvcController {
    private final AddonService addonService;
    private final UserService userService;
    private final AddonMapper addonMapper;
    private final TagService tagService;
    private final TargetIdeService targetIdeService;
    private final CategoryService categoryService;

    public AddonMvcController(AddonService addonService,
                              UserService userService,
                              AddonMapper addonMapper,
                              TagService tagService,
                              TargetIdeService targetIdeService,
                              CategoryService categoryService) {
        this.addonService = addonService;
        this.userService = userService;
        this.addonMapper = addonMapper;
        this.tagService = tagService;
        this.targetIdeService = targetIdeService;
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

    @ModelAttribute("allTags")
    public List<Tag> populateTags() {
        return tagService.getAll();
    }

    @ModelAttribute("allIdes")
    public List<TargetIde> populateIdes() {
        return targetIdeService.getAll();
    }

    @ModelAttribute("allCategories")
    public List<Category> populateCategories(){
        return categoryService.getAll();
    }

    @ModelAttribute("draftAddons")
    public List<Addon> populateDraftAddons() {
        return addonService.getAllDraftAddons();
    }

    @GetMapping("/{id}")
    public String getAddon(@PathVariable int id, Model model) {
        try {
            Addon addon = addonService.getAddonById(id);
            model.addAttribute("addon", addon);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
        return "approved_addon_view";
    }

    @GetMapping("/draft/{id}")
    public String getDraftAddon(@PathVariable int id, Model model) {
        try {
            Addon addon = addonService.getDraftById(id);
            model.addAttribute("draftAddon", addon);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
        return "draft_addon";
    }

    @GetMapping
    public String getAddons(Model model,
                            @ModelAttribute AddonFilter addonFilter) {
        Page<Addon> addons = addonService.getAllApproved(addonFilter.getSearch(),
                addonFilter.getTargetIde(),
                addonFilter.getCategory(),
                addonFilter.getSortBy(),
                addonFilter.getOrderBy(),
                addonFilter.getPage(),
                addonFilter.getSize());
        model.addAttribute("page", addons);
        model.addAttribute("addonFilterDto", new AddonFilterDto());

        int totalPages = addons.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        return "all_addons";
    }

    @GetMapping("/create")
    public String createAddon(Model model) {
//        TODO add User
        model.addAttribute("addon", new CreateAddonDto());
        return "create_addon";
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST, params = "action=publish")
    public String createAddon(@Valid @ModelAttribute("addon") CreateAddonDto addon,
                              @RequestParam("binaryContent") MultipartFile binaryContent,
                              BindingResult bindingResult,
                              Principal principal) {
        if (bindingResult.hasErrors()) {
            return "create_addon";
        }
        try {
            User user = getLoggedUser(principal);
            Addon addonToCreate = addonMapper.fromDtoCreate(addon, user);

            addonService.create(addonToCreate, binaryContent);
            return "pending";
        } catch (DuplicateEntityException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (GithubApiException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage() +
                    "Please check your repository url for typos. If the issue persists, contact Addonis support.");
        }

    }

    @RequestMapping(value = "/create", method = RequestMethod.POST, params = "action=save")
    public String createAddonDraft(@ModelAttribute("addon") CreateAddonDto createAddonDraftDto,
                                   @RequestParam("binaryContent") MultipartFile binaryContent,
                                   BindingResult bindingResult,
                                   Principal principal) {
        if (bindingResult.hasErrors()) {
            return "create_addon";
        }
        try {
            User user = getLoggedUser(principal);
            Addon addonDraftToCreate = addonMapper.fromDtoCreate(createAddonDraftDto, user);
            addonService.createDraft(addonDraftToCreate, binaryContent, user);
            return "redirect:/addons/user/" + user.getId();
        } catch (DuplicateEntityException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (GithubApiException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage() +
                    "Please check your repository url for typos. If the issue persists, contact Addonis support.");
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditApprovedOrPendingAddonView(@PathVariable int id,
                                                     Model model) {
        try {
            Addon addon = addonService.getApprovedOrPendingAddonById(id);
            UpdateAddonDto addonToUpdate = new UpdateAddonDto();
            addonMapper.transferData(addon, addonToUpdate);
            model.addAttribute("addon", addonToUpdate);
            model.addAttribute("existingAddon", addon);
            return "edit_addon";
        } catch(EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (UnauthorizedOperationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    @PostMapping("/{id}/edit")
    public String editApprovedOrPendingAddon(@PathVariable int id,
                            @Valid @ModelAttribute("newInput") UpdateAddonDto addonToUpdate,
                            @RequestParam("binaryContent") MultipartFile binaryContent,
                            BindingResult bindingResult,
                                             Principal principal) {
        if (bindingResult.hasErrors()) {
            return "edit_addon";
        }
        try {
            User user = getLoggedUser(principal);
            Addon addon = addonService.getApprovedOrPendingAddonById(id);
            addon = addonMapper.fromDtoUpdate(addonToUpdate, addon);

            addonService.update(addon, binaryContent, user);

            if (addon.getState().getName().equals("pending")) {
                return "redirect:/addons/pending/" + addon.getId();
            } else {
                return "redirect:/addons/" + addon.getId();
            }
        } catch(EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (UnauthorizedOperationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (DuplicateEntityException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @GetMapping("/{id}/edit-draft")
    public String showEditDraftAddonView(@PathVariable int id,
                                                     Model model) {
        try {
            Addon addon = addonService.getDraftById(id);
            CreateAddonDto draftAddonToUpdate = new CreateAddonDto();
            addonMapper.transferData(addon, draftAddonToUpdate);
            model.addAttribute("draftAddon", draftAddonToUpdate);
            model.addAttribute("existingDraftAddon", addon);
            return "edit_draft_addon";
        } catch(EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (UnauthorizedOperationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    @RequestMapping(value = "/{id}/edit-draft", method = RequestMethod.POST, params = "action=save")
    public String editDraftAddon(@PathVariable int id,
                                 @ModelAttribute("newInput") CreateAddonDto addonToUpdate,
                                 @RequestParam("binaryContent") MultipartFile binaryContent,
                                 BindingResult bindingResult,
                                 Principal principal) {
        if(bindingResult.hasErrors()) {
            return "edit_draft_addon";
        }
        try {
            User user = getLoggedUser(principal);
            Addon addon = addonService.getDraftById(id);
            addon = addonMapper.updateDraft(addonToUpdate, user, addon);

            addon = addonService.update(addon, binaryContent, user);

            return "redirect:/addons/draft/" + addon.getId();
        } catch(EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (UnauthorizedOperationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (DuplicateEntityException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @RequestMapping(value = "/{id}/edit-draft", method = RequestMethod.POST, params = "action=publish")
    public String createAddonFromDraft(@PathVariable int id,
                                       @Valid @ModelAttribute CreateAddonDto createAddonFromDraft,
                                       @RequestParam("binaryContent") MultipartFile binaryContent,
                                       BindingResult bindingResult,
                                       Principal principal) {
        if(bindingResult.hasErrors()) {
            return "edit_draft_addon";
        }
        try {
            User user = getLoggedUser(principal);
            Addon addon = addonService.getDraftById(id);
            addon = addonMapper.updateDraft(createAddonFromDraft, user, addon);
            addonService.createFromDraft(addon, binaryContent, user);

            return "redirect:/addons/user/" + user.getId();
        } catch (DuplicateEntityException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public String showUserAddons(@PathVariable int userId, Model model) {
        User user = userService.getById(userId);
        List<Addon> getUserDraftAddons = addonService.getDraftAddonsByUser(userId);
        List<Addon> getUserPendingAddons = addonService.getPendingAddonsByUser(userId);
        List<Addon> getUserApprovedAddons = addonService.getApprovedAddonsByUser(userId);
        model.addAttribute("userDraftAddons", getUserDraftAddons);
        model.addAttribute("userPendingAddons", getUserPendingAddons);
        model.addAttribute("userApprovedAddons", getUserApprovedAddons);
        model.addAttribute("user", user);

        return "user_addons";
    }
}
