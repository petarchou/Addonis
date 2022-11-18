package com.final_project.addonis.utils.exceptions;

public class SoftDeletedEntityException extends RuntimeException {
    public SoftDeletedEntityException(String type,
                                      String attribute,
                                      String attribute2,
                                      String value,
                                      String value2) {
        super(String.format("%s with %s '%s' or %s '%s' already exists and has been deleted. " +
                        "If you want to restore the account contact us for further instructions.", type,
                attribute, value, attribute2, value2));
    }
}
