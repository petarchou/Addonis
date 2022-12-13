package com.final_project.addonis.models.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
public class UsersFilter {
    private String search;
    private String filter;
    private String sort;
    private Boolean order;
    private Integer page;
    private Integer size;

    public UsersFilter() {
    }

    public UsersFilter(String search,
                       String filter,
                       String sort,
                       boolean order,
                       int page,
                       int size) {
        this.search = search;
        this.filter = filter;
        this.sort = sort;
        this.order = order;
        this.page = page;
        this.size = size;
    }
}
