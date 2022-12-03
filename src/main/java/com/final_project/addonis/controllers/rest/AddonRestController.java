package com.final_project.addonis.controllers.rest;

import com.final_project.addonis.models.*;
import com.final_project.addonis.models.dtos.*;
import com.final_project.addonis.services.contracts.AddonService;
import com.final_project.addonis.services.contracts.UserService;
import com.final_project.addonis.utils.exceptions.DuplicateEntityException;
import com.final_project.addonis.utils.exceptions.EntityNotFoundException;
import com.final_project.addonis.utils.exceptions.GithubApiException;
import com.final_project.addonis.utils.exceptions.UnauthorizedOperationException;
import com.final_project.addonis.utils.helpers.CategoryHelper;
import com.final_project.addonis.utils.helpers.TagHelper;
import com.final_project.addonis.utils.mappers.AddonMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/addons")
public class AddonRestController {
    private final AddonService addonService;
    private final AddonMapper addonMapper;
    private final TagHelper tagHelper;
    private final CategoryHelper categoryHelper;

    private final UserService userService;

    public AddonRestController(AddonService addonService,
                               AddonMapper addonMapper,
                               TagHelper tagHelper,
                               CategoryHelper categoryHelper,
                               UserService userService) {

        this.addonService = addonService;
        this.addonMapper = addonMapper;
        this.tagHelper = tagHelper;
        this.categoryHelper = categoryHelper;
        this.userService = userService;
    }

    @GetMapping
    public List<AddonDto> getAll(@RequestParam(value = "search", required = false) Optional<String> keyword,
                                 @RequestParam(value = "targetIde", required = false) Optional<String> targetIde,
                                 @RequestParam(value = "category", required = false) Optional<String> category,
                                 @RequestParam(value = "order", required = false) Optional<Boolean> order,
                                 @RequestParam(value = "page", required = false) Optional<Integer> page,
                                 @RequestParam(value = "size", required = false) Optional<Integer> size) {
        try {

            return addonService.getAllApproved(keyword, targetIde, category, order, page, size)
                    .stream()
                    .map(addonMapper::toDto)
                    .collect(Collectors.toList());

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public AddonDto get(@PathVariable int id) {
        try {
            Addon addon = addonService.getAddonById(id);
            return addonMapper.toDto(addon);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/featured")
    public List<AddonDto> getFeaturedAddons() {
        return addonService.getAddonsFeaturedByAdmin().stream()
                .map(addonMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/most-downloaded")
    public List<AddonDto> getMostDownloadedAddons() {
        return addonService.getMostDownloadedAddons().stream()
                .map(addonMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/recently-added")
    public List<AddonDto> getRecentlyAddedAddons() {
        return addonService.getNewestAddons().stream()
                .map(addonMapper::toDto)
                .collect(Collectors.toList());
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/pending")
    public List<AddonDto> getAllPending() {
        return addonService.getAllPendingAddons().stream()
                .map(addonMapper::toDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    public AddonDto create(@Valid @RequestPart CreateAddonDto createAddonDto,
                           @RequestParam("file") MultipartFile file,
                           Principal principal) {
        try {
            User user = userService.getByUsername(principal.getName());
            Addon addon = addonMapper.fromDto(createAddonDto, user);
            addon = addonService.create(addon, file);
            return addonMapper.toDto(addon);
        } catch (DuplicateEntityException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (GithubApiException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage() +
                    "Please check your repository url for typos. If the issue persists, contact Addonis support.");
        }
    }

    @PutMapping("/{id}")
    public AddonDto update(@Valid @RequestBody UpdateAddonDto updateAddonDto,
                           @PathVariable int id,
                           Principal principal) {
        try {
            User user = userService.getByUsername(principal.getName());
            Addon addon = addonService.getAddonById(id);
            addon = addonMapper.fromUpdateDto(updateAddonDto, addon);
            addon = addonService.update(addon, user);
            return addonMapper.toDto(addon);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (DuplicateEntityException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public AddonDto delete(@PathVariable int id, Principal principal) {
        try {
            User user = userService.getByUsername(principal.getName());
            Addon addon = addonService.getAddonById(id);
            addon = addonService.delete(addon.getId(), user);
            return addonMapper.toDto(addon);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping("/{id}/tags")
    public AddonDto addTags(@PathVariable int id,
                            @Valid @RequestBody TagDto tagDto,
                            Principal principal) {
        try {
            User user = userService.getByUsername(principal.getName());
            Addon addon = addonService.getAddonById(id);
            List<Tag> tags = tagDto.getTagNames().stream()
                    .map(tagHelper::fromTagName).collect(Collectors.toList());
            addonService.addTagsToAddon(addon, tags, user);
            return addonMapper.toDto(addon);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (UnauthorizedOperationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    @DeleteMapping("/{addonId}/tags/{tagId}")
    public AddonDto removeTag(@PathVariable int addonId,
                              @PathVariable int tagId,
                              Principal principal) {
        try {
            User user = userService.getByUsername(principal.getName());
            Addon addon = addonService.getAddonById(addonId);
            addonService.removeTag(addon, user, tagId);
            return addonMapper.toDto(addon);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (UnauthorizedOperationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    @Secured("ROLE_ADMIN")
    @PutMapping("/{id}/approve")
    public AddonDto approveAddon(@PathVariable int id,
                                 @Valid @RequestBody CategoryDto categoryDto) {
        try {
            List<Category> categories = categoryDto.getCategories().stream()
                    .map(categoryHelper::fromCategoryName).collect(Collectors.toList());
            Addon addonToApprove = addonService.approveAddon(id, categories);
            return addonMapper.toDto(addonToApprove);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (UnauthorizedOperationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    @Secured("ROLE_ADMIN")
    @PutMapping("/{id}/featured-add")
    public AddonDto addAddonToFeatured(@PathVariable int id) {
        try {
            Addon addon = addonService.getAddonById(id);
            addon = addonService.addAddonToFeatured(addon);
            return addonMapper.toDto(addon);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @Secured("ROLE_ADMIN")
    @PutMapping("/{id}/featured-remove")
    public AddonDto removeAddonFromFeatured(@PathVariable int id) {
        try {
            Addon addon = addonService.getAddonById(id);
            addon = addonService.removeAddonFromFeatured(addon);
            return addonMapper.toDto(addon);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @GetMapping("/{id}/download")
    public BinaryContent downloadContent(@PathVariable int id) {
        try {
            return addonService.downloadContent(id);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping("/{addonId}/rate/{ratingId}")
    public AddonDto rate(@PathVariable int addonId,
                         @PathVariable int ratingId,
                         Principal principal) {
        try {
            Addon addon = addonService.getAddonById(addonId);
            User user = userService.getByUsername(principal.getName());
            addon = addonService.rateAddon(addon, user, ratingId);
            return addonMapper.toDto(addon);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping("/{addonId}/removeRate")
    public AddonDto removeRate(@PathVariable int addonId,
                               Principal principal) {
        try {
            Addon addon = addonService.getAddonById(addonId);
            User user = userService.getByUsername(principal.getName());
            addon = addonService.removeRate(addon, user);
            return addonMapper.toDto(addon);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }
}
