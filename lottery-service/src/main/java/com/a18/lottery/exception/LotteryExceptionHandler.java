package com.a18.lottery.exception;

import com.a18.common.exception.CommonExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@ControllerAdvice
public class LotteryExceptionHandler extends CommonExceptionHandler {

  @ExceptionHandler(InvalidBetException.class)
  protected ResponseEntity handleInvalidBetException(InvalidBetException ex, WebRequest request) {
    return buildResponseEntity(request, HttpStatus.BAD_REQUEST, ex.msgKey, ex.params);
  }

  @ExceptionHandler(IssueFailedException.class)
  protected ResponseEntity handleIssueFailedException(IssueFailedException ex, WebRequest request) {
    return this.buildResponseEntity(
        request,
        HttpStatus.INTERNAL_SERVER_ERROR,
        ex.msgKey,
        ex.msgParams
    );
  }
}
