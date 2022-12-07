package com.final_project.addonis.utils.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GithubApiConfig {

    @Bean
    public RestTemplate githubTemplate(RestTemplateBuilder template) {
        return template.build();
    }

    @Bean
    public HttpEntity<String> githubGetEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(System.getenv("GITHUBTOKEN"));
        return new HttpEntity<>("", headers);
    }
}