package com.final_project.addonis.services.contracts;

import java.util.Map;

public interface GitHubService {
    Map getRepositoryInfo(String owner, String repositoryName);
}
