package com.final_project.addonis.services.contracts;

import com.final_project.addonis.models.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface AddonService {

    List<Addon> getAllApproved(Optional<String> keyword,
                               Optional<String> targetIde,
                               Optional<String> category,
                               Optional<Boolean> ascending,
                               Optional<Integer> page,
                               Optional<Integer> size);


    List<Addon> getAllPendingAddons();

    Addon getAddonById(int addonId);

    Addon create(Addon addon, MultipartFile file) throws IOException;

    Addon update(Addon addon, User user);

    Addon delete(int id, User user);

    Addon getByName(String name);

    void addTagsToAddon(Addon addon, List<Tag> tags, User user);

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

}
