package com.final_project.addonis.models.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
public class AddonFilterDto {
    private Optional<String> search;
    private Optional<String> targetIde;
    private Optional<String> category;
    private Optional<String> sortBy;
    private Optional<Boolean> orderBy;
    private Optional<Integer> page;
    private Optional<Integer> size;

    public AddonFilterDto(Optional<String> search,
                          Optional<String> targetIde,
                          Optional<String> category,
                          Optional<String> sortBy,
                          Optional<Boolean> orderBy,
                          Optional<Integer> page,
                          Optional<Integer> size) {
        this.search = search;
        this.targetIde = targetIde;
        this.category = category;
        this.sortBy = sortBy;
        this.orderBy = orderBy;
        this.page = page;
        this.size = size;
    }

    public AddonFilterDto() {
    }
}
