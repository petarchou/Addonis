package com.final_project.addonis.controllers.rest;

import com.final_project.addonis.models.Addon;
import com.final_project.addonis.models.BinaryContent;
import com.final_project.addonis.models.Tag;
import com.final_project.addonis.models.User;
import com.final_project.addonis.models.dtos.AddonDto;
import com.final_project.addonis.models.dtos.CreateAddonDto;
import com.final_project.addonis.models.dtos.TagDto;
import com.final_project.addonis.models.dtos.UpdateAddonDto;
import com.final_project.addonis.services.contracts.AddonService;
import com.final_project.addonis.services.contracts.UserService;
import com.final_project.addonis.utils.config.springsecurity.metaannotations.IsHimselfOrAdmin;
import com.final_project.addonis.utils.exceptions.DuplicateEntityException;
import com.final_project.addonis.utils.exceptions.EntityNotFoundException;
import com.final_project.addonis.utils.exceptions.UnauthorizedOperationException;
import com.final_project.addonis.utils.mappers.AddonMapper;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/addons")
public class AddonRestController {
    private final AddonService addonService;
    private final AddonMapper addonMapper;
    private final TagMapper tagMapper;

    private final UserService userService;

    public AddonRestController(AddonService addonService, AddonMapper addonMapper, TagMapper tagMapper, UserService userService) {
        this.addonService = addonService;
        this.addonMapper = addonMapper;
        this.tagMapper = tagMapper;
        this.userService = userService;
    }

    @GetMapping
    public List<AddonDto> getAll() {
        return addonService.getAllApprovedAddons().stream()
                .map(addonMapper::toDto)
                .collect(Collectors.toList());
    }

    @Secured("ADMIN")
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
            User user = userService.getById(14);
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

    @Secured("ADMIN")
    @PutMapping("/{id}/approve")
    public AddonDto approveAddon(@PathVariable int id) {
        try {
            Addon addonToApprove = addonService.approveAddon(id);
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
