package com.final_project.addonis.models.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
public class AddonFilter {
    private Optional<String> search;
    private Optional<String> targetIde;
    private Optional<String> category;
    private Optional<String> sortBy;
    private Optional<Boolean> orderBy;
    private Optional<Integer> page;
    private Optional<Integer> size;

    public AddonFilter() {
        this(null, null, null, null, null, null, null);
    }

    public AddonFilter(String search,
                       String targetIde,
                       String category,
                       String sortBy,
                       Boolean orderBy,
                       Integer page,
                       Integer size) {
        this.search = Optional.ofNullable(search);
        this.targetIde = Optional.ofNullable(targetIde);
        this.category = Optional.ofNullable(category);
        this.sortBy = Optional.ofNullable(sortBy);
        this.orderBy = Optional.ofNullable(orderBy);
        this.page = Optional.ofNullable(page);
        this.size = Optional.ofNullable(size);
    }
}
