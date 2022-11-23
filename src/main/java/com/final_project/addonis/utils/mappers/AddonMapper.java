package com.final_project.addonis.utils.mappers;

import com.final_project.addonis.models.Addon;
import com.final_project.addonis.models.State;
import com.final_project.addonis.models.User;
import com.final_project.addonis.models.dtos.AddonDto;
import com.final_project.addonis.models.dtos.CreateAddonDto;
import com.final_project.addonis.models.dtos.UpdateAddonDto;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
public class AddonMapper {

    public AddonDto toDto(Addon addon) {
        AddonDto addonDto = new AddonDto();
        addonDto.setId(addon.getId());
        addonDto.setName(addon.getName());
        addonDto.setTargetIde(addon.getTargetIde());
        addonDto.setCreator(addon.getCreator());
        addonDto.setDescription(addon.getDescription());
        addonDto.setBinaryContent(addon.getData());
        addonDto.setOriginUrl(addon.getOriginUrl());
        addonDto.setUploadedDate(addon.getUploadedDate());
        addonDto.setDownloads(addon.getDownloads());
        addonDto.setState(addon.getState());
        addonDto.setTags(addon.getTags());

        return addonDto;
    }

    public Addon fromDto(CreateAddonDto createAddonDto, User loggedUser) {
        Addon addon = new Addon();
        State state = new State();
        state.setId(2);

        addon.setName(createAddonDto.getName());
        addon.setTargetIde(createAddonDto.getTargetIde());
        addon.setCreator(loggedUser);
        addon.setDescription(createAddonDto.getDescription());
        addon.setOriginUrl(createAddonDto.getOriginUrl());
        addon.setState(state);
        addon.setTags(new HashSet<>());

        return addon;
    }

    public Addon fromUpdateDto(UpdateAddonDto updateAddonDto, Addon addon) {
        addon.setName(updateAddonDto.getName());
        addon.setDescription(updateAddonDto.getDescription());

        return addon;
    }
}
