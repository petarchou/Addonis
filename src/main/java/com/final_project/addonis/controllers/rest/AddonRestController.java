package com.final_project.addonis.controllers.rest;

import com.final_project.addonis.models.Addon;
import com.final_project.addonis.models.Tag;
import com.final_project.addonis.models.User;
import com.final_project.addonis.models.dtos.AddonDto;
import com.final_project.addonis.models.dtos.CreateAddonDto;
import com.final_project.addonis.models.dtos.TagDto;
import com.final_project.addonis.models.dtos.UpdateAddonDto;
import com.final_project.addonis.services.contracts.AddonService;
import com.final_project.addonis.services.contracts.UserService;
import com.final_project.addonis.utils.exceptions.DuplicateEntityException;
import com.final_project.addonis.utils.exceptions.EntityNotFoundException;
import com.final_project.addonis.utils.mappers.AddonMapper;
import com.final_project.addonis.utils.mappers.TagMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/addons")
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
        return addonService.getAll().stream()
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
        }
    }

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

    @PutMapping("/{addonId}/tags")
    public AddonDto addTags(@PathVariable int addonId,
                            @Valid @RequestBody TagDto tagDto) {
        try {
            Addon addon = addonService.getAddonById(addonId);
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
}
