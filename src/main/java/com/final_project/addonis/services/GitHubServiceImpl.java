package com.final_project.addonis.services;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static java.lang.String.format;

@Service
public class GitHubServiceImpl {
    private final RestTemplate template;
    private final static String MASTER_URL = "https://api.github.com/repos/%s/%s";

    public GitHubServiceImpl(RestTemplateBuilder template) {
        this.template = template.build();
    }

    public Map getGitHubRepositoryInfo(String owner, String repoName) {
        String currentUrl = format(MASTER_URL, owner, repoName);
        return template.getForObject(currentUrl, Map.class);
    }
}
