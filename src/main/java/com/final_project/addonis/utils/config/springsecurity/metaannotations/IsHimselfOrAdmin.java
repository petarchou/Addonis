package com.final_project.addonis.utils.config.springsecurity.metaannotations;


import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("#id == authentication.principal.id || hasRole('ADMIN')")
public @interface IsHimselfOrAdmin {
}
