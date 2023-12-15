package com.food.ordering.system.application.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ValidationException.class)
    public ErrorDto handleException(ValidationException exception) {
        if (exception instanceof ConstraintViolationException constraintViolationException) {
            String violations = extractViolations(constraintViolationException);
            log.error(violations, exception);
            return new ErrorDto(HttpStatus.BAD_REQUEST.getReasonPhrase(), violations);
        } else {
            String exceptionMessage = exception.getMessage();
            log.error(exceptionMessage, exception);
            return new ErrorDto(HttpStatus.BAD_REQUEST.getReasonPhrase(), exceptionMessage);
        }
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorDto handleException(Exception exception) {
        log.error(exception.getMessage(), exception);
        return new ErrorDto(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "Unexpected error.");
    }

    private String extractViolations(ConstraintViolationException constraintViolationException) {
        return constraintViolationException.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("--"));
    }
}
