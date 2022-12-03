package com.final_project.addonis.controllers.rest;

import com.final_project.addonis.models.*;
import com.final_project.addonis.models.dtos.*;
import com.final_project.addonis.models.Addon;
import com.final_project.addonis.models.BinaryContent;
import com.final_project.addonis.models.Tag;
import com.final_project.addonis.models.User;
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
import java.util.ArrayList;
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
    public List<AddonDtoOut> getAll(@RequestParam(value = "search", required = false) Optional<String> keyword,
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
    public AddonDtoOut get(@PathVariable int id) {
        try {
            Addon addon = addonService.getAddonById(id);
            return addonMapper.toDto(addon);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/drafts/{id}")
    public AddonDtoOut getDraft(@PathVariable int  id) {
        try {
            Addon addon = addonService.getAddonById(id);
            return addonMapper.toDto(addon);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }



    @PostMapping("/drafts")
    public DraftDtoOut createDraft(@RequestPart(value = "json", required = false) CreateAddonDto createAddonDto,
                                   @RequestParam(value = "file", required = false) MultipartFile file,
                                   Principal principal) {
        try {
            User user = userService.getByUsername(principal.getName());
            Addon addon = addonMapper.fromDtoCreate(createAddonDto, user);
            addon = addonService.createDraft(addon, file, user);
            return addonMapper.toDraftDto(addon);
            //TODO make it so that all exceptions are handled in the same place ( service )
        } catch (IOException e) {
            throw new RuntimeException(e);

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }


    @PutMapping("/drafts/{id}")
    public DraftDtoOut updateDraft(@PathVariable int id,
                                   @RequestPart(value = "json") CreateAddonDto addonDto,
                                   @RequestParam(value = "file", required = false) MultipartFile file,
                                   Principal principal) {
        try {
            User user = userService.getByUsername(principal.getName());
            Addon addon = addonService.getDraftById(id);
            addon = addonMapper.updateDraft(addonDto, user, addon);
            addon = addonService.update(addon, file, addon.getCreator());
            return addonMapper.toDraftDto(addon);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (DuplicateEntityException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }


    @PostMapping("/from-draft/{id}")
    public AddonDtoOut createAddonFromDraft(@PathVariable int id,
                                            @Valid @RequestPart(value = "json") CreateAddonDto createAddonDto,
                                            @RequestParam(value = "file") MultipartFile file,
                                            Principal principal) {
        try {
            User user = userService.getByUsername(principal.getName());
            Addon addon = addonService.getDraftById(id);
            addon = addonMapper.updateDraft(createAddonDto, user, addon);
            addon = addonService.createFromDraft(addon, file, user);
            return addonMapper.toDto(addon);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }


    @GetMapping("/featured")
    public List<AddonDtoOut> getFeaturedAddons() {
        return addonService.getAddonsFeaturedByAdmin().stream()
                .map(addonMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/most-downloaded")
    public List<AddonDtoOut> getMostDownloadedAddons() {
        return addonService.getMostDownloadedAddons().stream()
                .map(addonMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/recently-added")
    public List<AddonDtoOut> getRecentlyAddedAddons() {
        return addonService.getNewestAddons().stream()
                .map(addonMapper::toDto)
                .collect(Collectors.toList());
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/pending")
    public List<AddonDtoOut> getAllPending() {
        return addonService.getAllPendingAddons().stream()
                .map(addonMapper::toDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    public AddonDtoOut create(@Valid @RequestPart(value = "json") CreateAddonDto createAddonDto,
                              @RequestParam("file") MultipartFile file,
                              Principal principal) {
        try {
            User user = userService.getByUsername(principal.getName());
            Addon addon = addonMapper.fromDtoCreate(createAddonDto, user);
            addon = addonService.create(addon, file);
            return addonMapper.toDto(addon);
        } catch (DuplicateEntityException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (GithubApiException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage() +
                    "Please check your repository url for typos. If the issue persists, contact Addonis support.");
        }
    }

    @PutMapping("/{id}")
    public AddonDtoOut update(@PathVariable int id, @Valid @RequestPart(value = "json") UpdateAddonDto addonDto,
                              @RequestParam(value = "file", required = false) MultipartFile file,
                              Principal principal) {
        try {
            User user = userService.getByUsername(principal.getName());
            Addon addon = addonService.getAddonById(id);
            addon = addonMapper.fromDtoUpdate(addonDto, addon);
            addon = addonService.update(addon,file, user);
            return addonMapper.toDto(addon);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (DuplicateEntityException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public AddonDtoOut delete(@PathVariable int id, Principal principal) {
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
    public AddonDtoOut addTags(@PathVariable int id,
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
    public AddonDtoOut removeTag(@PathVariable int addonId,
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
    public AddonDtoOut approveAddon(@PathVariable int id,
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
    public AddonDtoOut addAddonToFeatured(@PathVariable int id) {
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
    public AddonDtoOut removeAddonFromFeatured(@PathVariable int id) {
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
    public AddonDtoOut rate(@PathVariable int addonId,
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
    public AddonDtoOut removeRate(@PathVariable int addonId,
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
