package com.final_project.addonis.models;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
public class GithubCommit implements Serializable {

    private String message;

    private LocalDateTime date;

}
