package com.iglooclub.nungil.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TokenErrorResult implements ErrorResult {

    REFRESH_TOKEN_NOT_FOUND(HttpStatus.BAD_REQUEST, "Failed to find the refresh token in cookie"),
    UNEXPECTED_TOKEN(HttpStatus.BAD_REQUEST, "Given token is not valid"),
    INVALID_AUTHENTICATION(HttpStatus.UNAUTHORIZED, "Authentication failed for some reason. " +
            "(e.g. expired or wrong access token, access token not found, etc.)")
    ;

    private final HttpStatus httpStatus;
    private final String message;
}