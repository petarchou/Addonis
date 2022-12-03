package com.final_project.addonis.models.dtos;


import com.final_project.addonis.models.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class AddonDtoOut {

    private int id;

    private String name;

    private String targetIde;

    private UserDto creator;

    private String description;

    private BinaryContent binaryContent;

    private String originUrl;

    private String uploadedDate;

    private int downloads;

    private String state;

    private Set<String> tags;

    private Set<String> categories;

    private Map<String, Integer> rating;

    private Double averageRating;

    private int pullRequests;

    private int issuesCount;

    private String lastCommitMessage;

    private String lastCommitDate;

    private boolean isFeatured;

    public AddonDtoOut() {
    }
}
