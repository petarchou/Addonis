package com.final_project.addonis.controllers.mvc;

import com.final_project.addonis.models.Addon;
import com.final_project.addonis.models.BinaryContent;
import com.final_project.addonis.services.contracts.AddonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
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
import java.util.ArrayList;
import java.util.List;


@Controller
@RequestMapping
public class HomeMvcController {

    private final AddonService addonService;

    @Autowired
    public HomeMvcController(AddonService addonService) {
        this.addonService = addonService;
    }


    @ModelAttribute("sortAddon")
    public List<String> populateSortParams(){
        return new ArrayList<>(List.of("Ascending", "Descending"));
    }
    @GetMapping("/")
    public String showHomepage(Model model) {
        List<Addon> mostDownloadedAddons = addonService.getMostDownloadedAddons();
        List<Addon> newestAddons = addonService.getNewestAddons();
        List<Addon> featuredAddons = addonService.getAddonsFeaturedByAdmin();
        model.addAttribute("featuredAddons", featuredAddons);
        model.addAttribute("mostDownloadedAddons", mostDownloadedAddons);
        model.addAttribute("newestAddons", newestAddons);
        return "dashboard";
    }

    @GetMapping("/download")
    public void downloadAddon(@Param("id") int id, HttpServletResponse response) {
        try {
            BinaryContent binaryContent = addonService.downloadContent(id);
//            Optional<BinaryContent> result = Optional.ofNullable(binaryContent);
//            binaryContent = result.get();

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
}
