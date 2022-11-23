package com.final_project.addonis.services.contracts;

import com.final_project.addonis.models.Addon;
import com.final_project.addonis.models.Tag;
import com.final_project.addonis.models.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AddonService {

    List<Addon> getAll();

    Addon getAddonById(int addonId);

    Addon create(Addon addon, List<Tag> tags, MultipartFile file) throws IOException;

    Addon update(Addon addon, User user);

    Addon delete(int id, User user);

    Addon getByName(String name);

    Addon addTagsToAddon(Addon addon, List<Tag> tags);
}
