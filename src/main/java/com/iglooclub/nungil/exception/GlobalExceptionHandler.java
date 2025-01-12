package com.iglooclub.nungil.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * MethodArgumentNotValidException를 통해 발생한 필드 에러(@Valid)를 처리하는 메서드이다.
     *
     * @param ex      the exception
     * @param headers the headers to be written to the response
     * @param status  the selected response status
     * @param request the current request
     * @return 에러가 발생한 필드 목록과 HTTP Bad Request 반환
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request) {

        final List<String> errorList = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());

        log.warn("Invalid DTO Parameter errors : {}", errorList);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.create(HttpStatus.BAD_REQUEST.toString(), errorList.toString()));
    }

    @ExceptionHandler({
            GeneralException.class
    })
    public ResponseEntity<ErrorResponse> handleRestApiException(final CustomException exception) {
        ErrorResult errorResult = exception.getErrorResult();

        log.warn(errorResult.name() + " Exception occurred: ", exception);
        return this.makeErrorResponseEntity(errorResult);
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ErrorResponse> handleException(final Exception exception) {
        log.warn("Exception occur: ", exception);
        return this.makeErrorResponseEntity(GlobalErrorResult.UNKNOWN_EXCEPTION, exception.getMessage());
    }

    private ResponseEntity<ErrorResponse> makeErrorResponseEntity(final ErrorResult errorResult, final String message) {
        return ResponseEntity.status(errorResult.getHttpStatus())
                .body(ErrorResponse.create(errorResult.name(), message));
    }

    private ResponseEntity<ErrorResponse> makeErrorResponseEntity(final ErrorResult errorResult) {
        return ResponseEntity.status(errorResult.getHttpStatus())
                .body(ErrorResponse.create(errorResult));
    }

}
