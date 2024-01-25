package com.iglooclub.nungil.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberErrorResult implements ErrorResult {

    ANONYMOUS_USER(HttpStatus.UNAUTHORIZED, "Anonymous user"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "Failed to find the User"),
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
