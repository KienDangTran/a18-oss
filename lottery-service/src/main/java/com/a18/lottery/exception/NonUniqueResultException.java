package com.a18.lottery.exception;

public class NonUniqueResultException extends RuntimeException {
  public NonUniqueResultException(String msg) {
    super(msg);
  }
}
