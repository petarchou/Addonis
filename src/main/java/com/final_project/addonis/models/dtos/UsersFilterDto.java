package com.final_project.addonis.models.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
public class UsersFilterDto {
    private Optional<String> search;
    private Optional<String> filterByField;
    private Optional<String> sortByField;
    private Optional<Boolean> order;
    private Optional<Integer> page;
    private Optional<Integer> size;

    public UsersFilterDto() {
    }

    public UsersFilterDto(String search,
                          String filter,
                          String sort,
                          Boolean order,
                          Integer page,
                          Integer size) {

        this.search = Optional.ofNullable(search);
        this.filterByField = Optional.ofNullable(filter);
        this.sortByField = Optional.ofNullable(sort);
        this.order = Optional.ofNullable(order);
        this.page = Optional.ofNullable(page);
        this.size = Optional.ofNullable(size);
    }
}
