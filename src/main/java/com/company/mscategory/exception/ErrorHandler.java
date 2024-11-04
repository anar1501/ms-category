package com.company.mscategory.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.company.mscategory.exception.ExceptionConstraints.METHOD_NOT_ALLOWED_CODE;
import static com.company.mscategory.exception.ExceptionConstraints.METHOD_NOT_ALLOWED_CODE_MESSAGE;
import static com.company.mscategory.exception.ExceptionConstraints.UNEXPECTED_EXCEPTION_CODE;
import static com.company.mscategory.exception.ExceptionConstraints.UNEXPECTED_EXCEPTION_MESSAGE;
import static com.company.mscategory.exception.ExceptionConstraints.VALIDATION_EXCEPTION_CODE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public ExceptionResponse handle(Exception ex) {
        log.error("Exception: ", ex);
        return new ExceptionResponse(UNEXPECTED_EXCEPTION_CODE, UNEXPECTED_EXCEPTION_MESSAGE);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(NOT_FOUND)
    public ExceptionResponse handle(NotFoundException ex) {
        log.error("NotFoundException: ", ex);
        return new ExceptionResponse(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(CannotDeleteSubCategoryException.class)
    @ResponseStatus(BAD_REQUEST)
    public ExceptionResponse handle(CannotDeleteSubCategoryException ex) {
        log.error("CannotDeleteSubCategoryException: ", ex);
        return new ExceptionResponse(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(BAD_REQUEST)
    public ExceptionResponse handle(MethodArgumentNotValidException ex) {
        log.error("MethodArgumentNotValidException: ", ex);
        var errorMessage = new StringBuilder("Validation failed: ");
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errorMessage.append(String.format("[%s: %s] ", error.getField(), error.getDefaultMessage()))
        );
        return new ExceptionResponse(VALIDATION_EXCEPTION_CODE, errorMessage.toString().trim());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(METHOD_NOT_ALLOWED)
    public ExceptionResponse handle(HttpRequestMethodNotSupportedException ex) {
        log.error("HttpRequestMethodNotSupportedException: ", ex);
        return new ExceptionResponse(METHOD_NOT_ALLOWED_CODE, METHOD_NOT_ALLOWED_CODE_MESSAGE);
    }

}
