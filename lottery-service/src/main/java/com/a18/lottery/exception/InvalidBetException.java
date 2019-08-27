package com.a18.lottery.exception;

public class InvalidBetException extends RuntimeException {
  String msgKey;

  Object[] params;

  public InvalidBetException(String msgKey, Object[] params) {
    super(msgKey);
    this.msgKey = msgKey;
    this.params = params;
  }
}
