package com.final_project.addonis.services.contracts;

import com.final_project.addonis.models.GithubCommit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface GitHubService {

    int getPullRequests(String owner,String repo);

    int getIssuesCount(String owner, String repo);

    GithubCommit getLastCommit(String owner, String repo);

    List<String> getRepoDetailsIfValid(String url);

}
