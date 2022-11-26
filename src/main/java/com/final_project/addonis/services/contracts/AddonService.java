package com.final_project.addonis.services.contracts;

import com.final_project.addonis.models.Addon;
import com.final_project.addonis.models.BinaryContent;
import com.final_project.addonis.models.Tag;
import com.final_project.addonis.models.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AddonService {
    List<Addon> getAllApprovedAddons();

    List<Addon> getAllPendingAddons();

    Addon getAddonById(int addonId);

    Addon create(Addon addon, List<Tag> tags, MultipartFile file) throws IOException;

    Addon update(Addon addon, User user);

    Addon delete(int id, User user);

    Addon getByName(String name);


    void addTagsToAddon(Addon addon, List<Tag> tags);

    Addon approveAddon(int id);

    BinaryContent downloadContent(int addonId);


    Addon rateAddon(Addon addon, User user, int rating);

    Addon removeRate(Addon addon, User user);

}
