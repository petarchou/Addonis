package com.final_project.addonis.models.dtos;


import com.final_project.addonis.models.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
public class AddonDto {

    private int id;

    private String name;

    private TargetIde targetIde;

    private User creator;

    private String description;

    private BinaryContent binaryContent;

    private String originUrl;

    private LocalDateTime uploadedDate;

    private int downloads;

    private State state;

    private Set<Tag> tags;

    public AddonDto() {
    }
}