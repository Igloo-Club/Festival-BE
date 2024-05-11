package com.iglooclub.nungil.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorResponse {
    private final String code;
    private final String message;

    public static ErrorResponse create(ErrorResult errorResult) {
        return new ErrorResponse(errorResult.name(), errorResult.getMessage());
    }

    public static ErrorResponse create(String code, String message) {
        return new ErrorResponse(code, message);
    }
}
