package com.final_project.addonis.services;

import com.final_project.addonis.models.*;
import com.final_project.addonis.repositories.contracts.AddonRepository;
import com.final_project.addonis.repositories.contracts.RatingRepository;
import com.final_project.addonis.repositories.contracts.StateRepository;
import com.final_project.addonis.repositories.contracts.TagRepository;
import com.final_project.addonis.services.contracts.BinaryContentService;
import com.final_project.addonis.services.contracts.CategoryService;
import com.final_project.addonis.services.contracts.GitHubService;
import com.final_project.addonis.services.contracts.TargetIdeService;
import com.final_project.addonis.utils.exceptions.BlockedUserException;
import com.final_project.addonis.utils.exceptions.DuplicateEntityException;
import com.final_project.addonis.utils.exceptions.EntityNotFoundException;
import com.final_project.addonis.utils.exceptions.UnauthorizedOperationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AddonServiceImplTests {

    @Mock
    private AddonRepository addonRepository;
    @Mock
    private BinaryContentService binaryContentService;
    @Mock
    private RatingRepository ratingRepository;
    @Mock
    private StateRepository stateRepository;
    @Mock
    private CategoryService categoryService;
    @Mock
    private GitHubService gitHubService;
    @Mock
    private TargetIdeService targetIdeService;
    @Mock
    private TagRepository tagRepository;
    @InjectMocks
    private AddonServiceImpl addonService;

    @Test
    public void getAllApproved_should_return_allApprovedAddons() {
        // Arrange
        Page<Addon> repoList = new PageImpl<>(List.of(new Addon(), new Addon())
                , PageRequest.of(0, 2), 2);
        when(addonRepository.findAllAddonsByFilteringAndSorting(any(),
                any(),
                any(),
                any(),
                anyBoolean(),
                anyInt(),
                anyInt()))
                .thenReturn(repoList);

        // Act
        Page<Addon> serviceList = addonService.getAllApproved(Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of("name"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());

        // Assert
        assertSame(repoList, serviceList);
    }

    @Test
    public void getAll_should_throwsException_when_targetIdeNotExist() {
        // Arrange
        when(targetIdeService.existByName(anyString())).thenReturn(false);
        // , Act, Assert
        assertThrows(IllegalArgumentException.class, () -> // Act
                addonService.getAllApproved(Optional.empty(),
                        Optional.of("test"),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));
    }

    @Test
    public void getAll_should_throwsException_when_sortByIsNotValid() {
        // Arrange, Act, Assert
        assertThrows(IllegalArgumentException.class, () -> // Act
                addonService.getAllApproved(Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.of("test"),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));
    }

    @Test
    public void getAll_should_throwsException_when_categoryNotExist() {
        // Arrange
        when(categoryService.existByName(anyString())).thenReturn(false);
        // , Act, Assert
        assertThrows(IllegalArgumentException.class, () -> // Act
                addonService.getAllApproved(Optional.empty(),
                        Optional.empty(),
                        Optional.of("test"),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));
    }

    @Test
    public void getAllPendingAddons_should_returnAllAddonsWhitStatePending() {
        // Arrange
        List<Addon> repoList = List.of(new Addon(), new Addon());
        when(addonRepository.getAllByStateNamePending()).thenReturn(repoList);

        // Act
        List<Addon> serviceList = addonService.getAllPendingAddons();

        // Assert
        assertSame(repoList, serviceList);

    }

    @Test
    public void getAddonsFeaturedByAdmin_should_returnAllWhitFeaturedTrue() {
        // Arrange
        List<Addon> repoList = List.of(new Addon(), new Addon());
        when(addonRepository.getAddonsByFeaturedTrue()).thenReturn(repoList);

        // Act
        List<Addon> serviceList = addonService.getAddonsFeaturedByAdmin();

        // Assert
        assertSame(repoList, serviceList);

    }

    @Test
    public void getMostDownloadedAddons_should_returnAllApprovedOrderedByDownloads() {
        // Arrange
        List<Addon> repoList = List.of(new Addon(), new Addon());
        when(addonRepository.getAllByStateNameApprovedOrderByDownloads()).thenReturn(repoList);

        // Act
        List<Addon> serviceList = addonService.getMostDownloadedAddons();

        // Assert
        assertSame(repoList, serviceList);

    }

    @Test
    public void getNewestAddons_should_returnAllApprovedNewestAddons() {
        // Arrange
        List<Addon> repoList = List.of(new Addon(), new Addon());
        when(addonRepository.getAllByStateNameApprovedOrderByUploadedDate())
                .thenReturn(repoList);

        // Act
        List<Addon> serviceList = addonService.getNewestAddons();

        // Assert
        assertSame(repoList, serviceList);

    }

    @Test
    public void getById_should_returnAddon_when_addonExist() {
        // Arrange
        Addon mockAddon = new Addon();
        mockAddon.setId(1);
        when(addonRepository.findAddonByIdAndStateNameApproved(anyInt())).thenReturn(Optional.of(mockAddon));

        //Act, Assert
        assertEquals(addonService.getAddonById(1).getId(), 1);
        verify(addonRepository, times(1))
                .findAddonByIdAndStateNameApproved(anyInt());

    }

    @Test
    public void getById_should_throwsException_when_addonNotExist() {
        // Arrange
        when(addonRepository.findAddonByIdAndStateNameApproved(anyInt()))
                .thenReturn(Optional.empty());

        //Act, Assert
        assertThrows(EntityNotFoundException.class,
                () -> addonService.getAddonById(1));
        verify(addonRepository, times(1))
                .findAddonByIdAndStateNameApproved(anyInt());

    }

    @Test
    public void getDraftById_should_returnAddon_when_addonExist() {
        // Arrange
        Addon mockAddon = new Addon();
        mockAddon.setId(1);
        when(addonRepository.findByStateEqualsAndIdEquals(any(), anyInt()))
                .thenReturn(Optional.of(mockAddon));

        //Act, Assert
        assertEquals(addonService.getDraftById(1).getId(), 1);
        verify(addonRepository, times(1))
                .findByStateEqualsAndIdEquals(any(), anyInt());
    }

    @Test
    public void getDraftById_should_throwsException_when_addonNotExist() {
        // Arrange
        when(addonRepository.findByStateEqualsAndIdEquals(any(), anyInt()))
                .thenReturn(Optional.empty());

        //Act, Assert
        assertThrows(EntityNotFoundException.class,
                () -> addonService.getDraftById(1));
        verify(addonRepository, times(1))
                .findByStateEqualsAndIdEquals(any(), anyInt());

    }

    @Test
    public void getByName_should_returnAddon_when_addonExist() {
        // Arrange
        Addon mockAddon = new Addon();
        mockAddon.setId(1);
        when(addonRepository.findByName(anyString()))
                .thenReturn(Optional.of(mockAddon));

        //Act, Assert
        assertEquals(addonService.getByName(anyString()).getId(), 1);
        verify(addonRepository, times(1))
                .findByName(anyString());
    }

    @Test
    public void addAddonToFeatured_should_addAddonToFeatured_when_addonFeaturedFalse() {
        // Arrange
        Addon mockAddon1 = new Addon();
        mockAddon1.setId(1);
        mockAddon1.setFeatured(false);
        Addon mockAddon2 = new Addon();
        mockAddon2.setId(1);
        mockAddon2.setFeatured(true);
        when(addonRepository.saveAndFlush(any())).thenReturn(mockAddon2);

        // Act, Assert
        assertTrue(addonService.addAddonToFeatured(mockAddon1).isFeatured());
    }

    @Test
    public void addAddonToFeatured_should_throwsException_when_addonFeaturedTrue() {
        // Arrange
        Addon mockAddon = new Addon();
        mockAddon.setId(1);
        mockAddon.setFeatured(true);


        // Act, Assert
        assertThrows(IllegalArgumentException.class,
                () -> addonService.addAddonToFeatured(mockAddon));
    }

    @Test
    public void removeAddonFromFeatured_should_removeAddonFromFeatured_when_addonFeaturedTrue() {
        // Arrange
        Addon mockAddon1 = new Addon();
        mockAddon1.setId(1);
        mockAddon1.setFeatured(true);
        Addon mockAddon2 = new Addon();
        mockAddon2.setId(1);
        mockAddon2.setFeatured(false);
        when(addonRepository.saveAndFlush(any())).thenReturn(mockAddon2);

        // Act, Assert
        assertFalse(addonService.removeAddonFromFeatured(mockAddon1).isFeatured());
    }

    @Test
    public void removeAddonFromFeatured_should_throwsException_when_addonFeaturedFalse() {
        // Arrange
        Addon mockAddon = new Addon();
        mockAddon.setId(1);
        mockAddon.setFeatured(false);


        // Act, Assert
        assertThrows(IllegalArgumentException.class,
                () -> addonService.removeAddonFromFeatured(mockAddon));
    }

    @Test
    public void create_should_createAddon_when_addonDetailsIsValid() {
        // Arrange
        Addon mockAddon = new Addon();
        mockAddon.setName("testName");
        User mockCreator = mock(User.class);
        mockCreator.setBlocked(false);
        mockAddon.setCreator(mockCreator);
        when(binaryContentService.store(any())).thenReturn(new BinaryContent());
        when(gitHubService.getRepoDetailsIfValid(any())).thenReturn(List.of("test", "test"));
        when(gitHubService.getIssuesCount(anyString(), anyString())).thenReturn(1);
        when(gitHubService.getLastCommit(anyString(), anyString())).thenReturn(new GithubCommit());
        when(gitHubService.getPullRequests(anyString(), anyString())).thenReturn(1);
        when(addonRepository.saveAndFlush(any())).thenReturn(mockAddon);
        when(addonRepository.findByNameAndStateNameIgnoreCaseNot(anyString(), anyString()))
                .thenReturn(Optional.empty());

        // Act
        addonService.create(mockAddon, new MockMultipartFile("test", new byte[]{1, 2}));

        // Assert
        verify(addonRepository, times(1))
                .saveAndFlush(any());
    }

    @Test
    public void create_should_throwsException_when_addonNameExists() {
        // Arrange
        Addon mockAddon1 = new Addon();
        mockAddon1.setId(1);
        mockAddon1.setName("testName");
        User mockCreator = mock(User.class);
        mockCreator.setBlocked(false);
        mockAddon1.setCreator(mockCreator);
        Addon mockAddon2 = new Addon();
        mockAddon2.setName("testName");
        when(addonRepository.findByNameAndStateNameIgnoreCaseNot(anyString(), any()))
                .thenReturn(Optional.of(mockAddon2));


        // Act, Assert
        assertThrows(DuplicateEntityException.class,
                () -> addonService.create(mockAddon1,
                        new MockMultipartFile("test", new byte[]{1, 2})));

    }

    @Test
    public void create_should_throwsException_when_addonCreatorIsBlocked() {
        // Arrange
        Addon mockAddon = new Addon();
        mockAddon.setName("testName");
        User mockCreator = new User();
        mockCreator.setBlocked(true);
        mockAddon.setCreator(mockCreator);


        // Act, Assert
        assertThrows(BlockedUserException.class,
                () -> addonService.create(mockAddon,
                        new MockMultipartFile("test", new byte[]{1, 2})));

    }

    @Test
    public void create_should_throwException_when_addonDetailsIsInvalid() {
        // Arrange
        Addon mockAddon = new Addon();
        mockAddon.setName("testName");
        User mockCreator = mock(User.class);
        mockCreator.setBlocked(false);
        mockAddon.setCreator(mockCreator);
        when(binaryContentService.store(any())).thenReturn(new BinaryContent());

        // Act, Assert
        assertThrows(Exception.class,
                () -> addonService.create(mockAddon, new MockMultipartFile("test", new byte[]{1, 2})));
    }

    @Test
    public void createDraft_should_createDraftAddon_when_addonDetailsIsValid() {
        // Arrange
        User mockCreator = new User();
        Addon mockAddon = new Addon();
        mockAddon.setCreator(mockCreator);
        when(addonRepository.saveAndFlush(any())).thenReturn(mockAddon);

        // Act
        Addon serviceAddon = addonService.createDraft(mockAddon,
                new MockMultipartFile("test", new byte[]{1, 2}),
                mockCreator);

        // Assert
        assertEquals(mockAddon, serviceAddon);
    }

    @Test
    public void createDraft_should_throwsException_when_userIsNotCreator() {
        // Arrange
        User mockCreator = new User();
        mockCreator.setId(1);
        mockCreator.setRoles(new HashSet<>());
        Addon mockAddon = new Addon();
        mockAddon.setCreator(new User());


        // Act, Assert
        assertThrows(UnauthorizedOperationException.class,
                () -> addonService.createDraft(mockAddon,
                        new MockMultipartFile("test", new byte[]{1, 2}),
                        mockCreator));
    }

    @Test
    public void createDraft_should_notUpdateDetails_when_multiPartFileIsEmpty() {
        // Arrange
        User mockCreator = new User();
        Addon mockAddon = new Addon();
        mockAddon.setCreator(mockCreator);
        when(addonRepository.saveAndFlush(any())).thenReturn(mockAddon);

        // Act
        Addon serviceAddon = addonService.createDraft(mockAddon,
                new MockMultipartFile("test", new byte[0]),
                mockCreator);

        // Assert
        assertEquals(mockAddon, serviceAddon);
    }

    @Test
    public void createDraft_should_updateDetails_when_fileIsNew() {
        // Arrange
        User mockCreator = new User();
        Addon mockAddon = new Addon();
        mockAddon.setId(1);
        mockAddon.setCreator(mockCreator);
        mockAddon.setData(new BinaryContent("data", new byte[]{3, 4}));
        when(addonRepository.saveAndFlush(any())).thenReturn(new Addon());

        // Act
        Addon serviceAddon = addonService.createDraft(mockAddon,
                new MockMultipartFile("test", new byte[]{1, 2}),
                mockCreator);

        // Assert
        assertNotEquals(mockAddon, serviceAddon);
    }

    @Test
    public void createFromDraft_should_createAddon_when_addDetailsIsValid() {
        // Arrange
        Addon mockAddon = new Addon();
        mockAddon.setName("testName");
        User mockCreator = mock(User.class);
        mockCreator.setBlocked(false);
        mockAddon.setCreator(mockCreator);
        State mockState = new State();
        mockState.setName("pending");
        mockState.setId(2);
        when(stateRepository.findByName(anyString())).thenReturn(mockState);
        when(binaryContentService.store(any())).thenReturn(new BinaryContent());
        when(gitHubService.getRepoDetailsIfValid(any())).thenReturn(List.of("test", "test"));
        when(gitHubService.getIssuesCount(anyString(), anyString())).thenReturn(1);
        when(gitHubService.getLastCommit(anyString(), anyString())).thenReturn(new GithubCommit());
        when(gitHubService.getPullRequests(anyString(), anyString())).thenReturn(1);
        when(addonRepository.findByNameAndStateNameIgnoreCaseNot(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(addonRepository.saveAndFlush(any())).thenReturn(mockAddon);

        // Act
        addonService.createFromDraft(mockAddon,
                new MockMultipartFile("test", new byte[]{1, 2}),
                mockCreator);

        // Assert
        verify(addonRepository, times(1))
                .saveAndFlush(any());
    }

    @Test
    public void delete_should_deleteAddon_when_detailsIsValid() {
        // Arrange
        Addon mockAddon = new Addon();
        mockAddon.setName("testName");
        mockAddon.setRating(new HashMap<>());
        mockAddon.setCategories(new HashSet<>());
        mockAddon.setTags(new HashSet<>());
        User mockCreator = mock(User.class);
        mockCreator.setBlocked(false);
        mockAddon.setCreator(mockCreator);
        doNothing().when(addonRepository).delete(mockAddon);

        // Act
        addonService.delete(mockAddon, mockCreator);

        // Assert
        verify(addonRepository, times(1)).delete(any());
    }

    @Test
    public void deleteDraft_should_deleteDraftAddon_when_detailsIsValid() {
        // Arrange
        Addon mockAddon = new Addon();
        mockAddon.setName("testName");
        mockAddon.setRating(new HashMap<>());
        mockAddon.setCategories(new HashSet<>());
        mockAddon.setTags(new HashSet<>());
        User mockCreator = mock(User.class);
        mockCreator.setBlocked(false);
        mockAddon.setCreator(mockCreator);
        doNothing().when(addonRepository).delete(mockAddon);

        // Act
        addonService.delete(mockAddon, mockCreator);

        // Assert
        verify(addonRepository, times(1)).delete(any());
    }

    @Test
    public void addTagsToAddon_should_addTags_when_detailsIsValid() {
        // Arrange
        Addon mockAddon = new Addon();
        mockAddon.setName("testName");
        mockAddon.setRating(new HashMap<>());
        mockAddon.setCategories(new HashSet<>());
        mockAddon.setTags(new HashSet<>());
        User mockCreator = mock(User.class);
        mockCreator.setBlocked(false);
        mockAddon.setCreator(mockCreator);
        when(addonRepository.saveAndFlush(any())).thenReturn(mockAddon);

        // Act
        Addon serviceAddon = addonService.addTagsToAddon(mockAddon,
                List.of(new Tag("testTag"), new Tag("testTag")), mockCreator);

        // Assert
        verify(addonRepository, times(1)).saveAndFlush(any());
        assertEquals(serviceAddon, mockAddon);
    }

    @Test
    public void removeTag_should_removeTagFromAddonTags_when_detailsIsValid() {
        // Arrange
        Addon mockAddon = new Addon();
        mockAddon.setName("testName");
        mockAddon.setRating(new HashMap<>());
        mockAddon.setCategories(new HashSet<>());
        mockAddon.setTags(new HashSet<>());
        User mockCreator = mock(User.class);
        mockCreator.setBlocked(false);
        mockAddon.setCreator(mockCreator);
        Tag mockTag = new Tag();
        mockTag.setId(1);
        mockAddon.getTags().add(mockTag);
        when(tagRepository.getReferenceById(anyInt())).thenReturn(mockTag);


        // Act
        addonService.removeTag(mockAddon, mockCreator, 1);

        // Assert
        verify(addonRepository, times(1)).saveAndFlush(any());

    }

    @Test
    public void removeTag_should_throwsException_when_addonTagsNotContainTheTag() {
        // Arrange
        Addon mockAddon = new Addon();
        mockAddon.setName("testName");
        mockAddon.setRating(new HashMap<>());
        mockAddon.setCategories(new HashSet<>());
        mockAddon.setTags(new HashSet<>());
        User mockCreator = mock(User.class);
        mockCreator.setBlocked(false);
        mockAddon.setCreator(mockCreator);
        Tag mockTag = new Tag();
        mockTag.setId(1);
        when(tagRepository.getReferenceById(anyInt())).thenReturn(mockTag);

        //Act, Assert
        assertThrows(EntityNotFoundException.class,
                () -> addonService.removeTag(mockAddon, mockCreator, 1));

    }

    @Test
    public void approveAddon_should_changeStateToApprove_when_detailsIsValid() {
        // Arrange
        Addon mockAddon = new Addon();
        mockAddon.setName("testName");
        mockAddon.setRating(new HashMap<>());
        mockAddon.setCategories(new HashSet<>());
        mockAddon.setTags(new HashSet<>());
        when(gitHubService.getRepoDetailsIfValid(any())).thenReturn(List.of("test","test"));
        when(gitHubService.getLastCommit(anyString(),anyString())).thenReturn(new GithubCommit());
        when(gitHubService.getPullRequests(anyString(),anyString())).thenReturn(3);
        when(gitHubService.getIssuesCount(anyString(),anyString())).thenReturn(2);
        when(addonRepository.findAddonByIdAndStateNamePending(anyInt())).thenReturn(Optional.of(mockAddon));


        // Act
        addonService.approveAddon(anyInt(), List.of(new Category(), new Category()));

        // Assert
        verify(addonRepository, times(1)).saveAndFlush(any());
    }

    @Test
    public void downloadContent_should_returnAddonsData_when_addonExist() {
        // Arrange
        Addon mockAddon = new Addon();
        mockAddon.setName("testName");
        mockAddon.setRating(new HashMap<>());
        mockAddon.setCategories(new HashSet<>());
        mockAddon.setTags(new HashSet<>());
        mockAddon.setData(new BinaryContent());
        when(addonRepository.findAddonByIdAndStateNameApproved(anyInt())).thenReturn(Optional.of(mockAddon));

        // Act
        BinaryContent serviceContent = addonService.downloadContent(anyInt());

        // Assert
        assertEquals(serviceContent, mockAddon.getData());
    }

    @Test
    public void rateAddon_should_addRateToAddonRatings_when_detailsIsValid() {
        // Arrange
        Addon mockAddon = new Addon();
        mockAddon.setName("testName");
        mockAddon.setRating(new HashMap<>());
        User mockCreator = mock(User.class);
        mockCreator.setBlocked(false);
        mockAddon.setCreator(mockCreator);
        Rating mockRating = new Rating();
        mockAddon.getRating().put(mockCreator, mockRating);
        when(ratingRepository.findById(anyInt())).thenReturn(Optional.of(mockRating));
        when(addonRepository.saveAndFlush(any())).thenReturn(mockAddon);

        // Act
        Addon serviceAddon = addonService.rateAddon(mockAddon, mockCreator, anyInt());

        // Assert
        assertTrue(serviceAddon.getRating().containsValue(mockRating));
    }

    @Test
    public void removeRate_should_removeRateFromAddonRatings_when_detailsIsValid() {
        // Arrange
        Addon mockAddon = new Addon();
        mockAddon.setName("testName");
        mockAddon.setRating(new HashMap<>());
        User mockCreator = mock(User.class);
        mockCreator.setBlocked(false);
        mockAddon.setCreator(mockCreator);
        Rating mockRating = new Rating();
        when(addonRepository.saveAndFlush(any())).thenReturn(mockAddon);

        // Act
        Addon serviceAddon = addonService.removeRate(mockAddon, mockCreator);

        // Assert
        assertFalse(serviceAddon.getRating().containsValue(mockRating));
    }

    @Test
    public void updateAllAddons_should_updateGitHubInfo_when_addonsStateIsApproved() {
        // Arrange
        List<Addon> addonList = List.of(new Addon(), new Addon());
        when(addonRepository.getAllByStateNameEqualsIgnoreCase(any())).thenReturn(addonList);
        when(gitHubService.getRepoDetailsIfValid(any())).thenReturn(List.of("test", "test"));
        when(gitHubService.getIssuesCount(anyString(), anyString())).thenReturn(1);
        when(gitHubService.getLastCommit(anyString(), anyString())).thenReturn(new GithubCommit());
        when(gitHubService.getPullRequests(anyString(), anyString())).thenReturn(1);

        // Act
        addonService.updateAllAddons();

        // Assert
        verify(addonRepository, times(2)).saveAndFlush(any());
    }
}