package com.final_project.addonis.utils.mappers;

import com.final_project.addonis.models.Addon;
import com.final_project.addonis.models.Category;
import com.final_project.addonis.models.Tag;
import com.final_project.addonis.models.TargetIde;
import com.final_project.addonis.models.User;
import com.final_project.addonis.models.dtos.*;
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


    public DraftDtoOut toDraftDto(Addon addon) {
        DraftDtoOut draftDtoOut = new DraftDtoOut();
        updateBaseDtoOut(draftDtoOut,addon);

        return draftDtoOut;

    }

    public AddonDtoOut toDto(Addon addon) {
        AddonDtoOut addonDtoOut = new AddonDtoOut();
        updateBaseDtoOut(addonDtoOut,addon);
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
        addonDtoOut.setDownloads(addon.getDownloads());
        addonDtoOut.setAverageRating(addon.getAverageRating());
        addonDtoOut.setPullRequests(addon.getPullRequests());
        if (addon.getLastCommitDate() != null) {
            addonDtoOut.setLastCommitDate(FORMATTER.format(addon.getLastCommitDate()));
        }
        addonDtoOut.setLastCommitMessage(addon.getLastCommitMessage());
        addonDtoOut.setIssuesCount(addon.getIssuesCount());
        addonDtoOut.setFeatured(addon.isFeatured());

        return addonDtoOut;
    }

    public Addon fromDtoCreate(CreateAddonDto addonDto, User loggedUser) {
        Addon addon = new Addon();
        updateBaseDtoIn(addonDto,addon);
        addon.setTags(addonDto.getTags().stream()
                .map(tagHelper::fromTagName)
                .collect(Collectors.toSet()));
        addon.setRating(new HashMap<>());
        addon.setCreator(loggedUser);

        return addon;
    }

    //TODO Why is logged user not being used? - check
    public Addon updateDraft(CreateAddonDto addonDto, User loggedUser, Addon draft) {

        updateBaseDtoIn(addonDto,draft);
        draft.setTags(addonDto.getTags().stream()
                .map(tagHelper::fromTagName)
                .collect(Collectors.toSet()));

        return draft;
    }

    public Addon fromDtoUpdate(UpdateAddonDto addonDto, Addon existingAddon) {
        updateBaseDtoIn(addonDto, existingAddon);
        return existingAddon;
    }

    private void updateBaseDtoIn(BaseAddonDtoIn addonDto, Addon existingAddon) {
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

    private void updateBaseDtoOut(BaseAddonDtoOut addonDtoOut, Addon addon) {
        addonDtoOut.setId(addon.getId());
        addonDtoOut.setName(addon.getName());
        if(addon.getTargetIde() != null)
            addonDtoOut.setTargetIde(addon.getTargetIde().getName());
        addonDtoOut.setCreator(userMapper.toDto(addon.getCreator()));
        addonDtoOut.setDescription(addon.getDescription());
//        addonDtoOut.setBinaryContent(addon.getData());
        addonDtoOut.setOriginUrl(addon.getOriginUrl());
        addonDtoOut.setUploadedDate(FORMATTER.format(addon.getUploadedDate()));
        addonDtoOut.setState(addon.getState().getName());
        addonDtoOut.setTags(addon.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.toSet()));
    }
}
