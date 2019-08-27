package com.a18.auth.helper;

import com.a18.common.exception.CommonExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class AuthExceptionHandler extends CommonExceptionHandler {

  @ExceptionHandler(InvalidGrantException.class)
  public ResponseEntity handleInvalidGrantException(WebRequest request, InvalidGrantException ex) {
    return this.buildResponseEntity(request, HttpStatus.FORBIDDEN, ex.getLocalizedMessage());
  }

  @ExceptionHandler(InvalidTokenException.class)
  public ResponseEntity handleInvalidTokenException(WebRequest request, InvalidTokenException ex) {
    return this.buildResponseEntity(request, HttpStatus.FORBIDDEN, ex.getLocalizedMessage());
  }
}
