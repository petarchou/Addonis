package com.final_project.addonis.models.dtos;


import com.final_project.addonis.models.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class AddonDtoOut extends BaseAddonDtoOut {

    private int downloads;

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
