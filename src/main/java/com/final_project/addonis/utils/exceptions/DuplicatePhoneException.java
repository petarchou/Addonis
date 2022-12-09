package com.final_project.addonis.utils.exceptions;

public class DuplicatePhoneException extends DuplicateEntityException{
    public DuplicatePhoneException(String message) {
        super(message);
    }

    public DuplicatePhoneException(String type, String attribute, String value) {
        super(type, attribute, value);
    }
}
