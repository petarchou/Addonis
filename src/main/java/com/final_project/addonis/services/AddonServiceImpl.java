package com.final_project.addonis.services;

import com.final_project.addonis.models.*;
import com.final_project.addonis.repositories.contracts.AddonRepository;
import com.final_project.addonis.repositories.contracts.RatingRepository;
import com.final_project.addonis.repositories.contracts.StateRepository;
import com.final_project.addonis.services.contracts.*;
import com.final_project.addonis.utils.exceptions.DuplicateEntityException;
import com.final_project.addonis.utils.exceptions.EntityNotFoundException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class AddonServiceImpl implements AddonService {

    private final AddonRepository addonRepository;
    private final BinaryContentService binaryContentService;
    private final RatingRepository ratingRepository;
    private final StateRepository stateRepository;
    private final CategoryService categoryService;
    private final GitHubService gitHubService;
    private final TargetIdeService targetIdeService;

    public AddonServiceImpl(AddonRepository addonRepository,
                            BinaryContentService binaryContentService,
                            RatingRepository ratingRepository,
                            StateRepository stateRepository,
                            CategoryService categoryService,
                            GitHubService gitHubService,
                            TargetIdeService targetIdeService1) {


        this.addonRepository = addonRepository;
        this.binaryContentService = binaryContentService;
        this.ratingRepository = ratingRepository;
        this.stateRepository = stateRepository;
        this.categoryService = categoryService;
        this.gitHubService = gitHubService;
        this.targetIdeService = targetIdeService1;
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
    public Addon getAddonById(int addonId) {
        return addonRepository.findAddonByIdAndStateNameApproved(addonId)
                .orElseThrow(() -> new EntityNotFoundException("Addon", addonId));
    }

    @Override
    public Addon getByName(String name) {
        return addonRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Addon", "name", name));
    }

    @Override
    public Addon create(Addon addon, MultipartFile file) throws IOException {
        verifyIsUniqueName(addon);
        BinaryContent binaryContent = binaryContentService.store(file);
        updateGithubDetails(addon);

        addon.setData(binaryContent);
        addon.setState(stateRepository.findByName("pending"));
        addon = addonRepository.saveAndFlush(addon);
        return addon;
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
    public Addon update(Addon addon) {
        verifyIsUniqueName(addon);
        return addonRepository.saveAndFlush(addon);
    }

    @Override
    public Addon delete(int id) {
        Addon addon = getAddonById(id);
        addonRepository.delete(addon);
        return addon;
    }

    @Override
    public void addTagsToAddon(Addon addon, List<Tag> tags) {
        addon.getTags().addAll(tags);
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

    public Addon rateAddon(Addon addon, User user, int ratingId) {
        Rating currentRating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new EntityNotFoundException("Rating", "value", String.valueOf(ratingId)));
        addon.getRating().put(user, currentRating);
        return update(addon);
    }

    @Override
    public Addon removeRate(Addon addon, User user) {
        addon.getRating().remove(user);
        return update(addon);
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


    private void verifyIsUniqueName(Addon addon) {
        if (addonRepository.existsByName(addon.getName())) {
            Addon existingAddon = getByName(addon.getName());
            if (!addon.equals(existingAddon)) {
                throw new DuplicateEntityException("Addon", "name", addon.getName());
            }
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
