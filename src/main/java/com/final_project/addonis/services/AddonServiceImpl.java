package com.final_project.addonis.services;

import com.final_project.addonis.models.Addon;
import com.final_project.addonis.models.BinaryContent;
import com.final_project.addonis.models.Tag;
import com.final_project.addonis.models.User;
import com.final_project.addonis.repositories.contracts.AddonRepository;
import com.final_project.addonis.repositories.contracts.StateRepository;
import com.final_project.addonis.services.contracts.AddonService;
import com.final_project.addonis.services.contracts.BinaryContentService;
import com.final_project.addonis.services.contracts.TagService;
import com.final_project.addonis.utils.exceptions.DuplicateEntityException;
import com.final_project.addonis.utils.exceptions.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class AddonServiceImpl implements AddonService {

    private final AddonRepository addonRepository;
    private final TagService tagService;
    private final BinaryContentService binaryContentService;
    private final StateRepository stateRepository;

    public AddonServiceImpl(AddonRepository addonRepository,
                            TagService tagService,
                            BinaryContentService binaryContentService, StateRepository stateRepository) {
        this.addonRepository = addonRepository;
        this.tagService = tagService;
        this.binaryContentService = binaryContentService;
        this.stateRepository = stateRepository;
    }

    @Override
    public List<Addon> getAllApprovedAddons() {
        return addonRepository.getAllByStateNameApproved();
    }

    @Override
    public List<Addon> getAllPendingAddons() {
        return addonRepository.getAllByStateNamePending();
    }

    @Override
    public Addon getAddonById(int addonId) {
        return addonRepository.findById(addonId)
                .orElseThrow(() -> new EntityNotFoundException("Addon", addonId));
    }

    @Override
    public Addon getByName(String name) {
        return addonRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Addon", "name", name));
    }

    @Override
    public Addon create(Addon addon, List<Tag> tags, MultipartFile file) throws IOException {
        verifyIsUniqueName(addon);
        BinaryContent binaryContent = binaryContentService.store(file);
        addon.setData(binaryContent);
        addon.setState(stateRepository.findByName("pending"));
        addTags(addon, tags);
        addon = addonRepository.saveAndFlush(addon);
        return addon;
    }

    @Override
    public Addon update(Addon addon, User user) {
        verifyIsUniqueName(addon);
        return addonRepository.saveAndFlush(addon);
    }

    @Override
    public Addon delete(int id, User user) {
        Addon addon = getAddonById(id);
        addonRepository.delete(addon);
        return addon;
    }

    @Override
    public void addTagsToAddon(Addon addon, List<Tag> tags) {
        addTags(addon, tags);
        addonRepository.saveAndFlush(addon);
    }

    @Override
    public Addon approveAddon(int id) {
        Addon addon = getAddonById(id);
        addon.setState(stateRepository.findByName("approved"));
        return addonRepository.saveAndFlush(addon);
    }

    @Override
    public BinaryContent downloadContent(int addonId) {
        Addon addon = getAddonById(addonId);
        addon.setDownloads(addon.getDownloads() + 1);
        addonRepository.saveAndFlush(addon);
        return addon.getData();
    }

    private void addTags(Addon addon, List<Tag> tags) {
        for (Tag tag : tags) {
            try {
                tag = tagService.getTagByName(tag.getName());
            } catch (EntityNotFoundException e) {
                tagService.create(tag);
            }
            if (!addon.getTags().contains(tag)) {
                addon.addTags(tag);
            }
        }
    }

    private void verifyIsUniqueName(Addon addon) {
        boolean exist = true;
        Addon existingAddon = new Addon();
        try {
            existingAddon = getByName(addon.getName());
        } catch (EntityNotFoundException e) {
            exist = false;
        }
        if (exist && !existingAddon.equals(addon)) {
            throw new DuplicateEntityException("Addon", "name", addon.getName());
        }
    }
}
