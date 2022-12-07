package com.final_project.addonis.services;

import com.final_project.addonis.models.GithubCommit;
import com.final_project.addonis.services.contracts.GitHubService;
import com.final_project.addonis.utils.exceptions.EntityNotFoundException;
import com.final_project.addonis.utils.exceptions.GithubApiException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
public class GitHubServiceImpl implements GitHubService {
    private static final String GITHUB_PREFIX = "https://github.com/";
    public static final String INVALID_REPOSITORY = "Invalid repository url";
    public static final String GITHUB_API_ERROR = "Error retrieving %s from git hub";
    private final static String MASTER_URL = "https://api.github.com/repos/%s/%s";
    private final RestTemplate template;

    private final HttpEntity<String> githubGetEntity;

    @Autowired
    public GitHubServiceImpl(RestTemplate template,
                             HttpEntity<String> githubGetEntity) {
        this.template = template;
        this.githubGetEntity = githubGetEntity;
    }


    @Override
    public List<String> getRepoDetailsIfValid(String url) {

        if (!url.startsWith(GITHUB_PREFIX)) {
            throw new IllegalArgumentException(INVALID_REPOSITORY);
        }

        String details = url.substring(GITHUB_PREFIX.length());
        List<String> repoDetails = Arrays.stream(details.split("/")).collect(Collectors.toList());
        if (repoDetails.size() != 2) {
            throw new IllegalArgumentException(INVALID_REPOSITORY);
        }
        return repoDetails;
    }


    @Override
    public int getIssuesCount(String owner, String repo) {
        return (Integer) getBaseRepositoryInfo(owner, repo).get("open_issues_count");
    }

    @Override
    public int getPullRequests(String owner, String repo) {
        try {
            String url = String.format(MASTER_URL, owner, repo) + "/pulls?state=open&per_page=100";
            ResponseEntity<String> raw = template.exchange(url, HttpMethod.GET, githubGetEntity, String.class);
            return countPullRequests(raw.getBody());
        } catch (JSONException | RestClientException e) {
            throw new GithubApiException(format(GITHUB_API_ERROR, "pull requests"));
        }


    }

    private int countPullRequests(String raw) {
        return new JSONArray(raw).length();
    }

    @Override
    public GithubCommit getLastCommit(String owner, String repo) {
        try {
            String url = String.format(MASTER_URL, owner, repo) + "/commits?per_page=1&page=1";
            ResponseEntity<String> raw = template.exchange(url, HttpMethod.GET, githubGetEntity, String.class);
            JSONArray arr = new JSONArray(raw.getBody());
            if (arr.length() == 0) {
                throw new EntityNotFoundException("No commits  were found for repository " + repo);
            }
            JSONObject object = new JSONObject(arr.get(0).toString());
            JSONObject commitInfo = (JSONObject) object.get("commit");
            JSONObject dateInfo = (JSONObject) commitInfo.get("author");


            return createCommitObject(commitInfo, dateInfo);

        } catch (JSONException | RestClientException e) {
            throw new GithubApiException(format(GITHUB_API_ERROR, "commits"));
        }

    }

    private GithubCommit createCommitObject(JSONObject commitInfo, JSONObject dateInfo) {
        GithubCommit commit = new GithubCommit();
        String date = dateInfo.get("date").toString();
        String message = commitInfo.get("message").toString().strip();
        //set max message length?
        commit.setMessage(message);
        date = date.substring(0, date.length() - 1);
        commit.setDate(LocalDateTime.parse(date, DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return commit;
    }


    private JSONObject getBaseRepositoryInfo(String owner, String repo) {
        try {
            String url = format(MASTER_URL, owner, repo);
            ResponseEntity<String> raw = template.exchange(url, HttpMethod.GET, githubGetEntity, String.class);
            return new JSONObject(raw.getBody());
        } catch (JSONException | RestClientException e) {
            throw new GithubApiException(format(GITHUB_API_ERROR, "commits"));
        }

    }

}
