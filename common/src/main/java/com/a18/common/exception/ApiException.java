package com.a18.common.exception;

public class ApiException extends RuntimeException {
  public String msgKey;

  public Object[] msgParams;

  public ApiException(String message) {
    super(message);
    this.msgKey = message;
  }

  public ApiException(String msgKey, Object... msgParams) {
    this.msgKey = msgKey;
    this.msgParams = msgParams;
  }
}
