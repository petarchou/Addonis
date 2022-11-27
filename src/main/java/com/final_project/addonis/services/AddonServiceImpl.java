package com.final_project.addonis.services;

import com.final_project.addonis.models.*;
import com.final_project.addonis.repositories.contracts.AddonRepository;
import com.final_project.addonis.repositories.contracts.RatingRepository;
import com.final_project.addonis.repositories.contracts.StateRepository;
import com.final_project.addonis.services.contracts.AddonService;
import com.final_project.addonis.services.contracts.BinaryContentService;
import com.final_project.addonis.services.contracts.CategoryService;
import com.final_project.addonis.services.contracts.TagService;
import com.final_project.addonis.utils.exceptions.DuplicateEntityException;
import com.final_project.addonis.utils.exceptions.EntityNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class AddonServiceImpl implements AddonService {

    private final AddonRepository addonRepository;
    private final TagService tagService;
    private final BinaryContentService binaryContentService;
    private final RatingRepository ratingRepository;
    private final StateRepository stateRepository;
    private final CategoryService categoryService;

    public AddonServiceImpl(AddonRepository addonRepository,
                            TagService tagService,
                            BinaryContentService binaryContentService,
                            RatingRepository ratingRepository,
                            StateRepository stateRepository, CategoryService categoryService) {
        this.addonRepository = addonRepository;
        this.tagService = tagService;
        this.binaryContentService = binaryContentService;
        this.ratingRepository = ratingRepository;
        this.stateRepository = stateRepository;
        this.categoryService = categoryService;
    }

    @Override
    public List<Addon> getAll(Optional<String> keyword,
                              Optional<String> filter,
                              Optional<String> sortBy,
                              Optional<Boolean> orderBy,
                              Optional<Integer> page,
                              Optional<Integer> size) {
        int pageOrDefault = page.orElse(0);
        int sizeOrDefault = size.orElse(10);
        String sortOrDefault = sortBy.orElse("id");
        boolean descOrder = orderBy.orElse(false);

        if (keyword.isEmpty()) {
            Pageable pageable = PageRequest.of(pageOrDefault, sizeOrDefault,
                    descOrder ? Sort.by(sortOrDefault).descending() : Sort.by(sortOrDefault));
            return addonRepository.getAllByStateNameApproved(pageable);
        }
        return addonRepository.findAllAddonsByFilteringAndSorting(keyword.get(),
                filter,
                sortOrDefault,
                descOrder,
                pageOrDefault,
                sizeOrDefault);
    }

    @Override
    public List<Addon> getAllPendingAddons() {
        return addonRepository.getAllByStateNamePending();
    }

    @Override
    public Addon getAddonById(int addonId) {
        return addonRepository.findAddonByIdAndStateNameApproved(addonId)
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
    public Addon approveAddon(int id, List<Category> categories) {
        Addon addon = addonRepository.findAddonByIdAndStateNamePending(id)
                .orElseThrow(() -> new EntityNotFoundException("Addon", id));
        addCategories(addon, categories);
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

    public Addon rateAddon(Addon addon, User user, int ratingId) {
        Rating currentRating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new EntityNotFoundException("Rating", "value", String.valueOf(ratingId)));
        addon.getRating().put(user, currentRating);
        return update(addon, user);
    }

    @Override
    public Addon removeRate(Addon addon, User user) {
        addon.getRating().remove(user);
        return update(addon, user);
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

    private void addCategories(Addon addon, List<Category> categories) {
        for (Category category : categories) {
            try {
                category = categoryService.getCategoryByName(category.getName());
            } catch (EntityNotFoundException e) {
                categoryService.create(category);
            }
            if (!addon.getCategories().contains(category)) {
                addon.addCategories(category);
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
