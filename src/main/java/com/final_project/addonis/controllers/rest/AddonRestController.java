package com.final_project.addonis.controllers.rest;

import com.final_project.addonis.models.*;
import com.final_project.addonis.models.dtos.*;
import com.final_project.addonis.services.contracts.AddonService;
import com.final_project.addonis.services.contracts.UserService;
import com.final_project.addonis.utils.config.springsecurity.metaannotations.IsHimselfOrAdmin;
import com.final_project.addonis.utils.exceptions.DuplicateEntityException;
import com.final_project.addonis.utils.exceptions.EntityNotFoundException;
import com.final_project.addonis.utils.exceptions.UnauthorizedOperationException;
import com.final_project.addonis.utils.mappers.AddonMapper;
import com.final_project.addonis.utils.mappers.CategoryMapper;
import com.final_project.addonis.utils.mappers.TagMapper;
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
    private final TagMapper tagMapper;
    private final CategoryMapper categoryMapper;

    private final UserService userService;

    public AddonRestController(AddonService addonService, AddonMapper addonMapper, TagMapper tagMapper, CategoryMapper categoryMapper, UserService userService) {
        this.addonService = addonService;
        this.addonMapper = addonMapper;
        this.tagMapper = tagMapper;
        this.categoryMapper = categoryMapper;
        this.userService = userService;
    }

    @GetMapping
    public List<AddonDto> getAll(@RequestParam(value = "search", required = false) Optional<String> keyword,
                                            @RequestParam(value = "filter", required = false) Optional<String> filter,
                                            @RequestParam(value = "sortBy", required = false) Optional<String> sortBy,
                                            @RequestParam(value = "desc", required = false) Optional<Boolean> desc,
                                            @RequestParam(value = "page", required = false) Optional<Integer> page,
                                            @RequestParam(value = "size", required = false) Optional<Integer> size) {
        return addonService.getAll(keyword, filter, sortBy, desc, page, size).stream()
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

    @GetMapping("/{id}")
    public AddonDto get(@PathVariable int id) {
        try {
            Addon addon = addonService.getAddonById(id);
            return addonMapper.toDto(addon);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping
    public AddonDto create(@Valid @RequestPart CreateAddonDto createAddonDto,
                           @RequestParam("file") MultipartFile file) {
        try {
            // TODO Principal
            User user = userService.getById(52);
            Addon addon = addonMapper.fromDto(createAddonDto, user);
            List<Tag> tags = createAddonDto.getTags().stream()
                    .map(tagMapper::fromTagName).collect(Collectors.toList());
            addon = addonService.create(addon, tags, file);
            return addonMapper.toDto(addon);
        } catch (DuplicateEntityException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @IsHimselfOrAdmin
    @PutMapping("/{id}")
    public AddonDto update(@Valid @RequestBody UpdateAddonDto updateAddonDto,
                           @PathVariable int id) {
        try {
            Addon addon = addonService.getAddonById(id);
            addon = addonMapper.fromUpdateDto(updateAddonDto, addon);
            addon = addonService.update(addon, addon.getCreator());
            return addonMapper.toDto(addon);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (DuplicateEntityException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @IsHimselfOrAdmin
    @DeleteMapping("/{id}")
    public AddonDto delete(@PathVariable int id) {
        try {
            User user = userService.getById(14);
            Addon addon = addonService.getAddonById(id);
            addon = addonService.delete(addon.getId(), user);
            return addonMapper.toDto(addon);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @IsHimselfOrAdmin
    @PutMapping("/{id}/tags")
    public AddonDto addTags(@PathVariable int id,
                            @Valid @RequestBody TagDto tagDto) {
        try {
            Addon addon = addonService.getAddonById(id);
            List<Tag> tags = tagDto.getTagNames().stream()
                    .map(tagMapper::fromTagName).collect(Collectors.toList());
            if (!tags.isEmpty()) {
                addonService.addTagsToAddon(addon, tags);
            }
            return addonMapper.toDto(addon);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Secured("ROLE_ADMIN")
    @PutMapping("/{id}/approve")
    public AddonDto approveAddon(@PathVariable int id,
                                 @Valid @RequestBody CategoryDto categoryDto) {
        try {
            List<Category> categories = categoryDto.getCategories().stream()
                    .map(categoryMapper::fromCategoryName).collect(Collectors.toList());
            Addon addonToApprove = addonService.approveAddon(id, categories);
            return addonMapper.toDto(addonToApprove);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (UnauthorizedOperationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
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
