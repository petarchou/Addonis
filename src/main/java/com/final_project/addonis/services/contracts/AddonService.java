package com.final_project.addonis.services.contracts;

import com.final_project.addonis.models.*;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface AddonService {

    Page<Addon> getAllApproved(Optional<String> keyword,
                               Optional<String> targetIde,
                               Optional<String> category,
                               Optional<String> sortBy, Optional<Boolean> ascending,
                               Optional<Integer> page,
                               Optional<Integer> size);


    Addon getApprovedOrPendingAddonById(int id);
    List<Addon> getAllPendingAddons();

    List<Addon> getAllDraftAddons();

    Addon getAddonById(int addonId);

    Addon getPendingAddonById(int addonId);

    Addon create(Addon addon, MultipartFile file);

    Addon update(Addon addon, MultipartFile file, User user);

    Addon delete(int id, User user);

    Addon getByName(String name);

    Addon addTagsToAddon(Addon addon, List<Tag> tags, User user);

    void removeTag(Addon addon, User user, int tagId);

    Addon approveAddon(int id, List<Category> categories);

    BinaryContent downloadContent(int addonId);


    Addon rateAddon(Addon addon, User user, int ratingId);

    Addon removeRate(Addon addon, User user);


    void updateAllAddons();

    List<Addon> getAddonsFeaturedByAdmin();

    Addon addAddonToFeatured(Addon addon);

    Addon removeAddonFromFeatured(Addon addon);

    List<Addon> getMostDownloadedAddons();

    List<Addon> getNewestAddons();

    Addon createFromDraft(Addon addon, MultipartFile file, User user);

    Addon getDraftById(int id);

    Addon createDraft(Addon addon, MultipartFile file, User user);

    List<Addon> getPendingAddonsByUser(int userId);

    List<Addon> getApprovedAddonsByUser(int userId);

    List<Addon> getDraftAddonsByUser(int userId);

    void notifyUser(User creator, Addon addon);
}
