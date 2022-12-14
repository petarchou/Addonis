package com.final_project.addonis.services;

import com.final_project.addonis.utils.exceptions.IllegalGithubArgumentException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class GitHubServiceImplTests {
    private static final String GITHUB_PREFIX = "https://github.com/";
    @Mock
    private RestTemplate template;
    @Mock
    private HttpEntity<String> httpEntity;

    @InjectMocks
    private GitHubServiceImpl gitHubService;

    @Test
    public void getRepoDetailsIfValid_should_returnListWithDetails_when_urlIsValid() {
        // Arrange
        String mockUrl = GITHUB_PREFIX + "test" + "/test";

        // Act
        List<String> mockResult = gitHubService.getRepoDetailsIfValid(mockUrl);

        // Assert
        Assertions.assertEquals(mockResult.size(), 2);
    }

    @Test
    public void getRepoDetailsIfValid_should_throwsException_when_urlIsInvalid() {
        // Arrange
        String mockUrl = "test" + "/test";

        // Act, Assert
        assertThrows(IllegalGithubArgumentException.class,
                () -> gitHubService.getRepoDetailsIfValid(mockUrl));
    }

    @Test
    public void getRepoDetailsIfValid_should_throwsException_when_urlContainsMoreThanOwnerAndRepoName() {
        // Arrange
        String mockUrl = GITHUB_PREFIX + "test" + "/test" + "/test";

        // Act, Assert
        assertThrows(IllegalGithubArgumentException.class,
                () -> gitHubService.getRepoDetailsIfValid(mockUrl));
    }

//    @Test
//    public void getIssuesCount_should_returnCountOfIssues_when_detailsIsValid() {
//        // Arrange
//        String owner = "test";
//        String repo = "test";
//        JSONObject testDetails = mock(JSONObject.class);
//        ResponseEntity<Object> mockEntity = new ResponseEntity<>
//                ("testBody", HttpStatus.OK);
//        when(template.exchange(
//                anyString(),
//                any(HttpMethod.class),
//                any(),
//                ArgumentMatchers.<Class<Object>>any()))
//                .thenReturn(mockEntity);
//
//        doReturn("2").when(testDetails).get(anyString());
//        // Act, Assert
//        assertEquals(gitHubService.getIssuesCount(owner, repo), 2);
//
//    }
}



