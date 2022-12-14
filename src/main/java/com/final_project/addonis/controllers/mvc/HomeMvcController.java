package com.final_project.addonis.controllers.mvc;

import com.final_project.addonis.models.Addon;
import com.final_project.addonis.models.BinaryContent;
import com.final_project.addonis.models.User;
import com.final_project.addonis.services.contracts.AddonService;
import com.final_project.addonis.services.contracts.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityNotFoundException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;


@Controller
@RequestMapping
public class HomeMvcController {

    private final AddonService addonService;

    private final UserService userService;

    @Autowired
    public HomeMvcController(AddonService addonService, UserService userService) {
        this.addonService = addonService;
        this.userService = userService;
    }


    @ModelAttribute("sortAddon")
    public List<String> populateSortParams(){
        return new ArrayList<>(List.of("Ascending", "Descending"));
    }
    @GetMapping("/")
    public String showHomepage(Model model) {
        List<Addon> topTenDownloadedAddons = addonService.getTopTenDownloadedAddons();
        List<Addon> topTenNewestAddons = addonService.getTopTenNewestAddons();
        List<Addon> featuredAddons = addonService.getAddonsFeaturedByAdmin();
        model.addAttribute("featuredAddons", featuredAddons);
        model.addAttribute("topTenMostDownloadedAddons", topTenDownloadedAddons);
        model.addAttribute("topTenNewestAddons", topTenNewestAddons);
        return "home";
    }

    @GetMapping("/download")
    public void downloadAddon(@Param("id") int id, HttpServletResponse response) {
        try {
            BinaryContent binaryContent = addonService.downloadContent(id);

            response.setContentType("application/octet-stream");
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=" + binaryContent.getName();

            response.setHeader(headerKey, headerValue);
            ServletOutputStream outputStream = response.getOutputStream();

            outputStream.write(binaryContent.getData());
            outputStream.close();
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @ModelAttribute("isAuth")
    private boolean isAuthenticated(@CurrentSecurityContext SecurityContext context) {
        Authentication authentication = context.getAuthentication();
        return authentication != null
                && !(authentication instanceof AnonymousAuthenticationToken)
                && authentication.isAuthenticated();
    }

    @ModelAttribute("loggedUser")
    private User getLoggedUser(Principal principal) {
        return principal == null
                ? null
                : userService.getByUsername(principal.getName());
    }
}
