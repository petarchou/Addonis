package com.final_project.addonis.utils.mappers;

import com.final_project.addonis.models.Addon;
import com.final_project.addonis.models.Category;
import com.final_project.addonis.models.Tag;
import com.final_project.addonis.models.TargetIde;
import com.final_project.addonis.models.User;
import com.final_project.addonis.models.dtos.BaseAddonDtoIn;
import com.final_project.addonis.models.dtos.AddonDtoOut;
import com.final_project.addonis.models.dtos.CreateAddonDto;
import com.final_project.addonis.models.dtos.UpdateAddonDto;
import org.springframework.beans.factory.annotation.Autowired;
import com.final_project.addonis.utils.helpers.TagHelper;
import com.final_project.addonis.services.contracts.TargetIdeService;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.stream.Collectors;

@Component
public class AddonMapper {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");
    private final TagHelper tagHelper;
    private final UserMapper userMapper;
    private final TargetIdeService targetIdeService;

    @Autowired
    public AddonMapper(TagHelper tagHelper, UserMapper userMapper,
                       TargetIdeService targetIdeService) {
        this.tagHelper = tagHelper;
        this.userMapper = userMapper;
        this.targetIdeService = targetIdeService;
    }

    public AddonDtoOut toDto(Addon addon) {
        AddonDtoOut addonDtoOut = new AddonDtoOut();
        addonDtoOut.setId(addon.getId());
        addonDtoOut.setName(addon.getName());
        addonDtoOut.setTargetIde(addon.getTargetIde().getName());
        addonDtoOut.setCreator(userMapper.toDto(addon.getCreator()));
        addonDtoOut.setDescription(addon.getDescription());
        addonDtoOut.setBinaryContent(addon.getData());
        addonDtoOut.setOriginUrl(addon.getOriginUrl());
        addonDtoOut.setUploadedDate(FORMATTER.format(addon.getUploadedDate()));
        addonDtoOut.setDownloads(addon.getDownloads());
        addonDtoOut.setState(addon.getState().getName());
        addonDtoOut.setTags(addon.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.toSet()));
        if (addon.getState().getName().equalsIgnoreCase("approved")) {
            addonDtoOut.setCategories(addon.getCategories()
                    .stream().
                    map(Category::getName)
                    .collect(Collectors.toSet()));
        }
        addonDtoOut.setRating(addon.getRating()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> entry.getKey().getUsername(),
                        entry -> entry.getValue().getId())));
        addonDtoOut.setAverageRating(addon.getAverageRating());
        addonDtoOut.setPullRequests(addon.getPullRequests());
        addonDtoOut.setLastCommitDate(FORMATTER.format(addon.getLastCommitDate()));
        addonDtoOut.setLastCommitMessage(addon.getLastCommitMessage());
        addonDtoOut.setIssuesCount(addon.getIssuesCount());
        addonDtoOut.setFeatured(addon.isFeatured());

        return addonDtoOut;
    }

    public Addon fromDtoCreate(CreateAddonDto addonDto, User loggedUser) {
        Addon addon = new Addon();
        updateBaseInformation(addonDto,addon);
        addon.setTags(addonDto.getTags().stream()
                .map(tagHelper::fromTagName)
                .collect(Collectors.toSet()));
        addon.setRating(new HashMap<>());
        addon.setCreator(loggedUser);

        return addon;
    }


    public Addon updateDraft(CreateAddonDto addonDto, User loggedUser, Addon draft) {

        updateBaseInformation(addonDto,draft);
        draft.setTags(addonDto.getTags().stream()
                .map(tagHelper::fromTagName)
                .collect(Collectors.toSet()));

        return draft;
    }

    public Addon fromDtoUpdate(UpdateAddonDto addonDto, Addon existingAddon) {
        updateBaseInformation(addonDto, existingAddon);
        return existingAddon;
    }

    private void updateBaseInformation(BaseAddonDtoIn addonDto, Addon existingAddon) {
        String ideName = addonDto.getTargetIde();
        String name = addonDto.getName();
        String description = addonDto.getDescription();
        String originUrl = addonDto.getOriginUrl();


        if (name != null) {
            existingAddon.setName(name);
        }
        if (description != null) {
            existingAddon.setDescription(addonDto.getDescription());
        }
        if (originUrl != null) {
            existingAddon.setOriginUrl(addonDto.getOriginUrl());
        }
        if(ideName != null) {
            TargetIde targetIde = targetIdeService.getByName(ideName);
            existingAddon.setTargetIde(targetIde);
        }
    }
}
