package com.final_project.addonis.services;

import com.final_project.addonis.models.*;
import com.final_project.addonis.repositories.contracts.AddonRepository;
import com.final_project.addonis.repositories.contracts.RatingRepository;
import com.final_project.addonis.repositories.contracts.StateRepository;
import com.final_project.addonis.repositories.contracts.TagRepository;
import com.final_project.addonis.services.contracts.*;
import com.final_project.addonis.utils.exceptions.BlockedUserException;
import com.final_project.addonis.utils.exceptions.DuplicateEntityException;
import com.final_project.addonis.utils.exceptions.EntityNotFoundException;
import com.final_project.addonis.utils.exceptions.UnauthorizedOperationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class AddonServiceImpl implements AddonService {
    private static final String NOT_AUTHORIZED_ERROR = "You are not authorized to modify this post.";
    private final AddonRepository addonRepository;
    private final BinaryContentService binaryContentService;
    private final RatingRepository ratingRepository;
    private final StateRepository stateRepository;
    private final CategoryService categoryService;
    private final GitHubService gitHubService;
    private final TargetIdeService targetIdeService;
    private final TagRepository tagRepository;

    public AddonServiceImpl(AddonRepository addonRepository,
                            BinaryContentService binaryContentService,
                            RatingRepository ratingRepository,
                            StateRepository stateRepository,
                            CategoryService categoryService,
                            GitHubService gitHubService,
                            TargetIdeService targetIdeService1, TagRepository tagRepository) {


        this.addonRepository = addonRepository;
        this.binaryContentService = binaryContentService;
        this.ratingRepository = ratingRepository;
        this.stateRepository = stateRepository;
        this.categoryService = categoryService;
        this.gitHubService = gitHubService;
        this.targetIdeService = targetIdeService1;
        this.tagRepository = tagRepository;
    }

    @Override
    public List<Addon> getAllApproved(Optional<String> keyword,
                                      Optional<String> targetIde,
                                      Optional<String> category,
                                      Optional<Boolean> ascending,
                                      Optional<Integer> page,
                                      Optional<Integer> size) {

        validateIde(targetIde);
        validateCategory(category);

        int pageOrDefault = page.orElse(0);
        int sizeOrDefault = size.orElse(10);
        boolean orderOrDefault = ascending.orElse(true);

        return addonRepository.findAllAddonsByFilteringAndSorting(keyword,
                targetIde,
                category,
                orderOrDefault,
                pageOrDefault,
                sizeOrDefault);
    }

    @Override
    public List<Addon> getAllPendingAddons() {
        return addonRepository.getAllByStateNamePending();
    }

    @Override
    public List<Addon> getAddonsFeaturedByAdmin() {
        return addonRepository.getAddonsByFeaturedTrue();
    }

    @Override
    public List<Addon> getMostDownloadedAddons() {
        return addonRepository.getAllByStateNameApprovedOrderByDownloads();
    }

    @Override
    public List<Addon> getNewestAddons() {
        return addonRepository.getAllByStateNameApprovedOrderByUploadedDate();
    }

    @Override
    public Addon getAddonById(int addonId) {
        return addonRepository.findAddonByIdAndStateNameApproved(addonId)
                .orElseThrow(() -> new EntityNotFoundException("Addon", addonId));
    }

    @Override
    public Addon getDraftById(int id) {
        State state = stateRepository.findByName("draft");
        return addonRepository.findByStateEqualsAndIdEquals(state, id).orElseThrow(
                () -> new EntityNotFoundException("Draft", id));
    }

    @Override
    public Addon getByName(String name) {
        return addonRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Addon", "name", name));
    }

    @Override
    public Addon addAddonToFeatured(Addon addon) {
        if (addon.isFeatured()) {
            throw new IllegalArgumentException("Addon is already added to featured list");
        }
        addon.setFeatured(true);
        return addonRepository.saveAndFlush(addon);
    }

    @Override
    public Addon removeAddonFromFeatured(Addon addon) {
        if (!addon.isFeatured()) {
            throw new IllegalArgumentException("Addon is not in featured list");
        }
        addon.setFeatured(false);
        return addonRepository.saveAndFlush(addon);
    }

    @Override
    public Addon create(Addon addon, MultipartFile file) {
        try {
            checkIfUserIsBlocked(addon);
            verifyIsUniqueName(addon);
            BinaryContent binaryContent = binaryContentService.store(file);
            updateGithubDetails(addon);

            addon.setData(binaryContent);
            addon.setState(stateRepository.findByName("pending"));
            return addonRepository.saveAndFlush(addon);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    private void updateGithubDetails(Addon addon) {
        List<String> repoDetails = gitHubService.getRepoDetailsIfValid(addon.getOriginUrl());
        String owner = repoDetails.get(0);
        String repo = repoDetails.get(1);
        GithubCommit lastCommit = gitHubService.getLastCommit(owner, repo);
        int issuesCount = gitHubService.getIssuesCount(owner, repo);
        int pullsCount = gitHubService.getPullRequests(owner, repo);

        addon.setLastCommitDate(lastCommit.getDate());
        addon.setLastCommitMessage(lastCommit.getMessage());
        addon.setIssuesCount(issuesCount);
        addon.setPullRequests(pullsCount);
    }

    @Override
    public Addon createDraft(Addon addon,
                             MultipartFile file, User user) throws IOException {

        checkModifyPermissions(addon, user);
        updateFileIfExists(addon, file);

        addon.setState(stateRepository.findByName("draft"));
        return addonRepository.saveAndFlush(addon);

    }

    @Override
    public Addon createFromDraft(Addon addon, MultipartFile file, User user) {
        checkModifyPermissions(addon, user);
        addon.setState(stateRepository.findByName("pending"));
        addon.setUploadedDate(LocalDateTime.now());
        updateGithubDetails(addon);
        return update(addon, file, user);
    }

    @Override
    public Addon update(Addon addon, MultipartFile file, User user) {
        try {
            checkIfUserIsBlocked(addon);
            checkModifyPermissions(addon, user);
            verifyIsUniqueName(addon);
            updateFileIfExists(addon, file);

            return addonRepository.saveAndFlush(addon);
        } catch (IOException e) {
            //TODO what do we do with this exception? it's thrown by the file implementation.
            throw new RuntimeException();
        }
    }


    @Override
    public Addon delete(int id, User user) {
        Addon addon = getAddonById(id);
        checkIfUserIsBlocked(addon);
        checkModifyPermissions(addon, user);
        addon.getRating().clear();
        addon.getCategories().clear();
        addon.getTags().clear();
        addonRepository.delete(addon);
        return addon;
    }

    @Override
    public void addTagsToAddon(Addon addon, List<Tag> tags, User user) {
        checkModifyPermissions(addon, user);
        addon.getTags().addAll(tags);
        addonRepository.saveAndFlush(addon);
    }

    @Override
    public void removeTag(Addon addon, User user, int tagId) {
        checkModifyPermissions(addon, user);
        Tag tag = tagRepository.getReferenceById(tagId);
        if (!addon.getTags().contains(tag)) {
            throw new EntityNotFoundException(String.format(
                    "Addon with id %d doesn't have a tag '%s'", addon.getId(), tag.getName()
            ));
        }
        addon.getTags().remove(tag);
        addonRepository.saveAndFlush(addon);
    }

    @Override
    public Addon approveAddon(int id, List<Category> categories) {
        Addon addon = addonRepository.findAddonByIdAndStateNamePending(id)
                .orElseThrow(() -> new EntityNotFoundException("Addon", id));
        addon.setState(stateRepository.findByName("approved"));
        addon.getCategories().addAll(categories);
        return addonRepository.saveAndFlush(addon);
    }

    @Override
    public BinaryContent downloadContent(int addonId) {
        Addon addon = getAddonById(addonId);
        addon.setDownloads(addon.getDownloads() + 1);
        addonRepository.saveAndFlush(addon);
        return addon.getData();
    }

    @Override
    public Addon rateAddon(Addon addon, User user, int ratingId) {
        Rating currentRating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new EntityNotFoundException("Rating", "value", String.valueOf(ratingId)));
        addon.getRating().put(user, currentRating);
        return addonRepository.saveAndFlush(addon);
    }

    @Override
    public Addon removeRate(Addon addon, User user) {
        addon.getRating().remove(user);
        return addonRepository.saveAndFlush(addon);
    }

    @Async
    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.MINUTES)
    @Override
    public void updateAllAddons() {
        List<Addon> approvedAddons = addonRepository.getAllByStateNameEqualsIgnoreCase("approved");
        approvedAddons.forEach(addon -> {
            updateGithubDetails(addon);
            addonRepository.saveAndFlush(addon);
        });
    }


    private void updateFileIfExists(Addon addon, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            BinaryContent newFile = binaryContentService.store(file);
            BinaryContent oldFile = addon.getData();
            if (oldFile != null && !oldFile.equals(newFile)) {
                binaryContentService.delete(addon.getData());
            }
            addon.setData(newFile);

        }
    }

    private void verifyIsUniqueName(Addon addon) {
        Optional<Addon> optional =
                addonRepository.findByNameAndStateNameIgnoreCaseNot(addon.getName(), "draft");
        if (optional.isPresent() && !optional.get().equals(addon))
            throw new DuplicateEntityException("Addon", "name", addon.getName());

    }

    private void checkModifyPermissions(Addon addon, User user) {
        if (!addon.getCreator().equals(user) && !user.isAdmin()) {
            throw new UnauthorizedOperationException(NOT_AUTHORIZED_ERROR);
        }
    }

    private void checkIfUserIsBlocked(Addon addon) {
        if (addon.getCreator().isBlocked()) {
            throw new BlockedUserException("Blocked users cannot create posts");
        }
    }

    private void validateCategory(Optional<String> category) {
        if (category.isPresent()) {
            if (!categoryService.existByName(category.get())) {
                throw new IllegalArgumentException(String.format
                        ("Category with name %s not exist", category.get()));
            }
        }
    }

    private void validateIde(Optional<String> targetIde) {
        if (targetIde.isPresent()) {
            if (!targetIdeService.existByName(targetIde.get())) {
                throw new IllegalArgumentException(String.format
                        ("Ide with name %s not exist", targetIde.get()));
            }
        }
    }
}
