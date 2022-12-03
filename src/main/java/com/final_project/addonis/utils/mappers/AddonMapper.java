package com.final_project.addonis.utils.mappers;

import com.final_project.addonis.models.Addon;
import com.final_project.addonis.models.Category;
import com.final_project.addonis.models.Tag;
import com.final_project.addonis.models.User;
import com.final_project.addonis.models.dtos.AddonDto;
import com.final_project.addonis.models.dtos.CreateAddonDto;
import com.final_project.addonis.models.dtos.UpdateAddonDto;
import org.springframework.beans.factory.annotation.Autowired;
import com.final_project.addonis.utils.helpers.TagHelper;
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

    @Autowired
    public AddonMapper(TagHelper tagHelper, UserMapper userMapper) {
        this.tagHelper = tagHelper;
        this.userMapper = userMapper;
    }

    public AddonDto toDto(Addon addon) {
        AddonDto addonDto = new AddonDto();
        addonDto.setId(addon.getId());
        addonDto.setName(addon.getName());
        addonDto.setTargetIde(addon.getTargetIde().getTargetIdeName());
        addonDto.setCreator(userMapper.toDto(addon.getCreator()));
        addonDto.setDescription(addon.getDescription());
        addonDto.setBinaryContent(addon.getData());
        addonDto.setOriginUrl(addon.getOriginUrl());
        addonDto.setUploadedDate(FORMATTER.format(addon.getUploadedDate()));
        addonDto.setDownloads(addon.getDownloads());
        addonDto.setState(addon.getState().getName());
        addonDto.setTags(addon.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.toSet()));
        if (addon.getState().getName().equalsIgnoreCase("approved")) {
            addonDto.setCategories(addon.getCategories()
                    .stream().
                    map(Category::getName)
                    .collect(Collectors.toSet()));
        }
        addonDto.setRating(addon.getRating()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> entry.getKey().getUsername(),
                        entry -> entry.getValue().getId())));
        addonDto.setAverageRating(addon.getAverageRating());
        addonDto.setPullRequests(addon.getPullRequests());
        addonDto.setLastCommitDate(FORMATTER.format(addon.getLastCommitDate()));
        addonDto.setLastCommitMessage(addon.getLastCommitMessage());
        addonDto.setIssuesCount(addon.getIssuesCount());
        addonDto.setFeatured(addon.isFeatured());

        return addonDto;
    }

    public Addon fromDto(CreateAddonDto createAddonDto, User loggedUser) {
        Addon addon = new Addon();

        addon.setName(createAddonDto.getName());
        addon.setTargetIde(createAddonDto.getTargetIde());
        addon.setCreator(loggedUser);
        addon.setDescription(createAddonDto.getDescription());
        addon.setOriginUrl(createAddonDto.getOriginUrl());
        addon.setTags(createAddonDto.getTags().stream()
                .map(tagHelper::fromTagName)
                .collect(Collectors.toSet()));
        addon.setRating(new HashMap<>());

        return addon;
    }

    public Addon fromUpdateDto(UpdateAddonDto updateAddonDto, Addon addon) {
        addon.setName(updateAddonDto.getName());
        addon.setDescription(updateAddonDto.getDescription());

        return addon;
    }
}
