package com.covoiturage.covoiturage.advice;

import com.covoiturage.covoiturage.exception.BusinessRuleException;
import com.covoiturage.covoiturage.exception.ForbiddenOperationException;
import com.covoiturage.covoiturage.exception.InvalidRequestException;
import com.covoiturage.covoiturage.exception.ResourceNotFoundException;
import com.covoiturage.covoiturage.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Date;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // System exceptions
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, WebRequest request){
        HttpStatus status = HttpStatus.BAD_REQUEST ;
        return ResponseEntity.status(status).body(buildTheError(ex,status.value(),request)) ;    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, WebRequest request){
        HttpStatus status = HttpStatus.BAD_REQUEST ;
        return ResponseEntity.status(status).body(buildTheError(ex,status.value(),request)) ;    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, WebRequest request){
        HttpStatus status = HttpStatus.BAD_REQUEST ;
        return ResponseEntity.status(status).body(buildTheError(ex,status.value(),request)) ;    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex, WebRequest request){
        HttpStatus status = HttpStatus.BAD_REQUEST ;
        return ResponseEntity.status(status).body(buildTheError(ex,status.value(),request)) ;
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException ex, WebRequest request){
        HttpStatus status = HttpStatus.NOT_FOUND ;
        return ResponseEntity.status(status).body(buildTheError(ex,status.value(),request)) ;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleException(Exception ex, WebRequest request){
        HttpStatus status = HttpStatus.BAD_REQUEST ;
        return ResponseEntity.status(status).body(buildTheError(ex,status.value(),request)) ;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex, WebRequest request){
        HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED ;
        return ResponseEntity.status(status).body(buildTheError(ex,status.value(),request)) ;
    }

    // My Own Exceptions
    @ExceptionHandler(BusinessRuleException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorResponse> handleBusinessRuleException(BusinessRuleException ex, WebRequest request){
        HttpStatus status = HttpStatus.CONFLICT ;
        return ResponseEntity.status(status).body(buildTheError(ex,status.value(),request)) ;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request){
        HttpStatus status = HttpStatus.NOT_FOUND ;
        return ResponseEntity.status(status).body(buildTheError(ex,status.value(),request)) ;
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ErrorResponse> handleForbiddenOperationException(ForbiddenOperationException ex, WebRequest request){
        HttpStatus status = HttpStatus.FORBIDDEN ;
        return ResponseEntity.status(status).body(buildTheError(ex,status.value(),request)) ;
    }

    @ExceptionHandler(InvalidRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleInvalidRequestException(InvalidRequestException ex, WebRequest request){
        HttpStatus status = HttpStatus.BAD_REQUEST ;
        return ResponseEntity.status(status).body(buildTheError(ex,status.value(),request)) ;
    }
















    public ErrorResponse buildTheError(Exception ex ,int status,WebRequest request){
        return ErrorResponse.builder()
                .timestamp(new Date())
                .statusCode(status)
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();
    }

}
