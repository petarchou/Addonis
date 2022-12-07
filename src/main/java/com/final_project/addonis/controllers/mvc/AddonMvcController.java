package com.final_project.addonis.controllers.mvc;

import com.final_project.addonis.models.Addon;
import com.final_project.addonis.models.Tag;
import com.final_project.addonis.models.TargetIde;
import com.final_project.addonis.models.User;
import com.final_project.addonis.models.dtos.CreateAddonDto;
import com.final_project.addonis.services.contracts.AddonService;
import com.final_project.addonis.services.contracts.TagService;
import com.final_project.addonis.services.contracts.TargetIdeService;
import com.final_project.addonis.services.contracts.UserService;
import com.final_project.addonis.utils.exceptions.DuplicateEntityException;
import com.final_project.addonis.utils.exceptions.EntityNotFoundException;
import com.final_project.addonis.utils.exceptions.GithubApiException;
import com.final_project.addonis.utils.mappers.AddonMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
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

    public AddonMvcController(AddonService addonService, UserService userService, AddonMapper addonMapper, TagService tagService, TargetIdeService targetIdeService) {
        this.addonService = addonService;
        this.userService = userService;
        this.addonMapper = addonMapper;
        this.tagService = tagService;
        this.targetIdeService = targetIdeService;
    }

    @ModelAttribute("allAddons")
    public Page<Addon> populateAddons(Optional<String> keyword,
                                      Optional<String> targetIde,
                                      Optional<String> category,
                                      Optional<String> sortBy,
                                      Optional<Boolean> ascending,
                                      Optional<Integer> page,
                                      Optional<Integer> size) {
        return addonService.getAllApproved(keyword, targetIde, category, sortBy, ascending, page, size);
    }

    @ModelAttribute("allTags")
    public List<Tag> populateTags() {
        return tagService.getAll();
    }

    @ModelAttribute("allIdes")
    public List<TargetIde> populateIdes() {
        return targetIdeService.getAll();
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

    @GetMapping
    public String getAddons(Model model,
                            Optional<String> keyword,
                            Optional<String> targetIde,
                            Optional<String> category,
                            Optional<String> sortBy,
                            Optional<Boolean> ascending,
                            Optional<Integer> page,
                            Optional<Integer> size) {
        Page<Addon> addons = addonService.getAllApproved(keyword,
                targetIde, category, sortBy, ascending, page, size);
        model.addAttribute("page", addons);

        int totalPages = addons.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        return "allAddons";
    }

    @GetMapping("/create")
    public String createAddon(Model model) {
//        TODO add User
        model.addAttribute("addon", new CreateAddonDto());
        return "createAddon";
    }

    @PostMapping("/create")
    public String createAddon(@Valid @ModelAttribute("addon") CreateAddonDto addon,
                              @RequestParam("binaryContent") MultipartFile binaryContent,
                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "createAddon";
        }
        try {
//            User user = userService.getByUsername(principal.getName());
            User user = userService.getById(74);
            Addon addonToCreate = addonMapper.fromDtoCreate(addon, user);

            addonService.create(addonToCreate, binaryContent);
        } catch (DuplicateEntityException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (GithubApiException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage() +
                    "Please check your repository url for typos. If the issue persists, contact Addonis support.");
        }
        return "pending";
    }
}
