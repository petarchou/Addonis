package com.final_project.addonis.utils.helpers;

import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class EmailHelper {
    public String getSiteUrl(HttpServletRequest request) {
        String siteUrl = request.getRequestURL().toString();
        return siteUrl.replace(request.getServletPath(), "");
    }
}
