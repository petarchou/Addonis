package com.final_project.addonis.utils.exceptions;

public class DuplicateAddonNameException extends DuplicateEntityException {

    public DuplicateAddonNameException(String message) {
        super(message);
    }

    public DuplicateAddonNameException(String type, String attribute, String value) {
        super(type, attribute, value);
    }
}
