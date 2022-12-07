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
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
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
    private final EmailService emailService;

    public AddonServiceImpl(AddonRepository addonRepository,
                            BinaryContentService binaryContentService,
                            RatingRepository ratingRepository,
                            StateRepository stateRepository,
                            CategoryService categoryService,
                            GitHubService gitHubService,
                            TargetIdeService targetIdeService1, TagRepository tagRepository, EmailService emailService) {


        this.addonRepository = addonRepository;
        this.binaryContentService = binaryContentService;
        this.ratingRepository = ratingRepository;
        this.stateRepository = stateRepository;
        this.categoryService = categoryService;
        this.gitHubService = gitHubService;
        this.targetIdeService = targetIdeService1;
        this.tagRepository = tagRepository;
        this.emailService = emailService;
    }

    @Override
    public Page<Addon> getAllApproved(Optional<String> keyword,
                                      Optional<String> targetIde,
                                      Optional<String> category,
                                      Optional<Boolean> ascending,
                                      Optional<Integer> page,
                                      Optional<Integer> size) {

        validateIde(targetIde);
        validateCategory(category);

        int pageOrDefault = page.orElse(1);
        int sizeOrDefault = size.orElse(8);
        boolean orderOrDefault = ascending.orElse(true);

        return addonRepository.findAllAddonsByFilteringAndSorting(keyword,
                targetIde,
                category,
                orderOrDefault,
                pageOrDefault,
                sizeOrDefault);
    }

    @Override
    public List<Addon> getPendingAddonsByUser(int userId) {
        return addonRepository.getPendingAddonsByUser(userId);
    }

    @Override
    public List<Addon> getApprovedAddonsByUser(int userId) {
        return addonRepository.getApprovedAddonsByUser(userId);
    }

    @Override
    public List<Addon> getDraftAddonsByUser(int userId) {
        return addonRepository.getDraftedAddonsByUser(userId);
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
    public Addon getPendingAddonById(int addonId) {
        return addonRepository.findAddonByIdAndStateNamePending(addonId)
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
        Addon clone = addon.toBuilder().isFeatured(true).build();
        return addonRepository.saveAndFlush(clone);
    }

    @Override
    public Addon removeAddonFromFeatured(Addon addon) {
        if (!addon.isFeatured()) {
            throw new IllegalArgumentException("Addon is not in featured list");
        }
        Addon clone = addon.toBuilder()
                .isFeatured(false)
                .build();
        return addonRepository.saveAndFlush(clone);
    }

    @Override
    public Addon create(Addon addon, MultipartFile file) {
        checkIfUserIsBlocked(addon);
        verifyIsUniqueName(addon);
        BinaryContent binaryContent = binaryContentService.store(file);
        Addon clone = updateGithubDetails(addon).toBuilder()
                .data(binaryContent)
                .state(stateRepository.findByName("pending")).build();
        return addonRepository.saveAndFlush(clone);
    }

    private Addon updateGithubDetails(Addon addon) {
        List<String> repoDetails = gitHubService.getRepoDetailsIfValid(addon.getOriginUrl());
        String owner = repoDetails.get(0);
        String repo = repoDetails.get(1);
        GithubCommit lastCommit = gitHubService.getLastCommit(owner, repo);
        int issuesCount = gitHubService.getIssuesCount(owner, repo);
        int pullsCount = gitHubService.getPullRequests(owner, repo);

        return addon.toBuilder().lastCommitDate(lastCommit.getDate())
                .lastCommitMessage(lastCommit.getMessage())
                .issuesCount(issuesCount)
                .pullRequests(pullsCount).build();
    }

    @Override
    public Addon createDraft(Addon addon,
                             MultipartFile file, User user) {

        checkModifyPermissions(addon, user);
        Addon clone = updateFileIfExists(addon, file).toBuilder()
                .state(stateRepository.findByName("draft")).build();
        return addonRepository.saveAndFlush(clone);

    }

    @Override
    public Addon createFromDraft(Addon addon, MultipartFile file, User user) {
        checkModifyPermissions(addon, user);
        Addon clone = addon.toBuilder()
                .state(stateRepository.findByName("pending"))
                .uploadedDate(LocalDateTime.now()).build();
        clone = updateGithubDetails(clone);
        return update(clone, file, user);
    }

    @Override
    public Addon update(Addon addon, MultipartFile file, User user) {
        checkIfUserIsBlocked(addon);
        checkModifyPermissions(addon, user);
        verifyIsUniqueName(addon);
        Addon clone = updateFileIfExists(addon, file);

        return addonRepository.saveAndFlush(clone);

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
    public Addon addTagsToAddon(Addon addon, List<Tag> tags, User user) {
        checkModifyPermissions(addon, user);
        Addon clone = addon.toBuilder()
                .tags(new HashSet<>(addon.getTags()))
                .build();
        clone.getTags().addAll(tags);
        return addonRepository.saveAndFlush(clone);
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
        Addon clone = addon.toBuilder()
                .tags(new HashSet<>(addon.getTags()))
                .build();
        clone.getTags().remove(tag);
        addonRepository.saveAndFlush(clone);
    }

    @Override
    public Addon approveAddon(int id, List<Category> categories) {
        Addon addon = addonRepository.findAddonByIdAndStateNamePending(id)
                .orElseThrow(() -> new EntityNotFoundException("Addon", id));
        addon.setState(stateRepository.findByName("approved"));
        addon.getCategories().addAll(categories);
        addon = updateGithubDetails(addon);
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
        Addon clone = addon.toBuilder()
                .rating(new HashMap<>(addon.getRating())).build();
        Rating currentRating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new EntityNotFoundException("Rating", "value", String.valueOf(ratingId)));
        clone.getRating().put(user, currentRating);
        return addonRepository.saveAndFlush(clone);
    }

    @Override
    public Addon removeRate(Addon addon, User user) {
        Addon clone = addon.toBuilder()
                .rating(new HashMap<>(addon.getRating())).build();
        clone.getRating().remove(user);
        return addonRepository.saveAndFlush(clone);
    }

    @Async
    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.MINUTES)
    @Override
    public void updateAllAddons() {
        List<Addon> approvedAddons = addonRepository.getAllByStateNameEqualsIgnoreCase("approved");
        approvedAddons.forEach(addon -> {
            addon = updateGithubDetails(addon);
            addonRepository.saveAndFlush(addon);
        });
    }

    @Override
    public void notifyUser(User creator, Addon addon) {
        addon.setState(stateRepository.findByName("draft"));
        getDraftAddonsByUser(creator.getId()).add(addon);
        addonRepository.saveAndFlush(addon);
        emailService.sendEmailForRejectedAddon(creator.getEmail(), buildNotificationMessage(creator.getUsername(), addon.getName()));
    }

    private String buildNotificationMessage(String name, String addonName) {
        return "<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c\">\n" +
                "\n" +
                "<span style=\"display:none;font-size:1px;color:#fff;max-height:0\"></span>\n" +
                "\n" +
                "  <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;min-width:100%;width:100%!important\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"100%\" height=\"53\" bgcolor=\"#0b0c0c\">\n" +
                "        \n" +
                "        <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;max-width:580px\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\">\n" +
                "          <tbody><tr>\n" +
                "            <td width=\"70\" bgcolor=\"#0b0c0c\" valign=\"middle\">\n" +
                "                <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td style=\"padding-left:10px\">\n" +
                "                  \n" +
                "                    </td>\n" +
                "                    <td style=\"font-size:28px;line-height:1.315789474;Margin-top:4px;padding-left:10px\">\n" +
                "                      <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block\">Check our website for awesome plugins!</span>\n" +
                "                    </td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "              </a>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "        </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"10\" height=\"10\" valign=\"middle\"></td>\n" +
                "      <td>\n" +
                "        \n" +
                "                <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td bgcolor=\"#1D70B8\" width=\"100%\" height=\"10\"></td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\" height=\"10\"></td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "\n" +
                "\n" +
                "\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "      <td style=\"font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:1.315789474;max-width:560px\">\n" +
                "        \n" +
                "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Hi " + name + ",</p><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> It seems that your recently uploaded addon: " + addonName + " was put in your list of DRAFTS by one of our admins. Check it again for typos or inappropriate wording and content.</p><blockquote style=\"Margin:0 0 20px 0;border-left:10px solid #b1b4b6;padding:15px 0 0.1px 15px;font-size:19px;line-height:25px\"><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"></p></blockquote>\n <p>For more information please contact our customer support!</p>" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "  </tbody></table><div class=\"yj6qo\"></div><div class=\"adL\">\n" +
                "\n" +
                "</div></div>";
    }

    private Addon updateFileIfExists(Addon addon, MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            BinaryContent newFile = binaryContentService.store(file);
            BinaryContent oldFile = addon.getData();
            if (oldFile != null && !oldFile.equals(newFile)) {
                binaryContentService.delete(addon.getData());
            }
            return addon.toBuilder().data(newFile).build();
        }
        return addon;
    }

    private void verifyIsUniqueName(Addon addon) {
        Optional<Addon> optional =
                addonRepository.findByNameAndStateNameIgnoreCaseNot(addon.getName(), "draft");
        if (optional.isPresent() && !optional.get().equals(addon)) {
            throw new DuplicateEntityException("Addon", "name", addon.getName());
        }
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
