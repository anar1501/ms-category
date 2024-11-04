package com.company.mscategory.exception;

import lombok.Getter;
@Getter
public class CannotDeleteSubCategoryException extends RuntimeException {
    private final String code;

    public CannotDeleteSubCategoryException(String message, String code) {
        super(message);
        this.code = code;
    }
}