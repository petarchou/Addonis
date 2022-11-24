package com.final_project.addonis.services;

import com.final_project.addonis.services.contracts.GitHubService;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static java.lang.String.format;

@Service
public class GitHubServiceImpl implements GitHubService {
    private final RestTemplate template;
    private final static String MASTER_URL = "https://api.github.com/repos/%s/%s";

    //add /commits or /commit
    //add /pulls

    public GitHubServiceImpl(RestTemplateBuilder template) {
        this.template = template.build();
    }

    @Override
    public Map getRepositoryInfo(String owner, String repositoryName) {
        String currentUrl = format(MASTER_URL, owner, repositoryName);
        return template.getForObject(currentUrl, Map.class);

    }
}
