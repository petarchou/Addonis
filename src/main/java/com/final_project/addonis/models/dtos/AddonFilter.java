package com.final_project.addonis.models.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddonFilter {
    private String search;
    private String targetIde;
    private String category;
    private String sortBy;
    private Boolean order;
    private Integer page;
    private Integer size;

    public AddonFilter() {
    }

    public AddonFilter(String search,
                       String targetIde,
                       String category,
                       String sortBy,
                       Boolean order,
                       Integer page,
                       Integer size) {
        this.search = search;
        this.targetIde = targetIde;
        this.category = category;
        this.sortBy = sortBy;
        this.order = order;
        this.page = page;
        this.size = size;
    }
}
