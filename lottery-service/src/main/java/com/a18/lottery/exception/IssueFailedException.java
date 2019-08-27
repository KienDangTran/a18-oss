package com.a18.lottery.exception;

import com.a18.common.exception.ApiException;
import java.util.Arrays;

public class IssueFailedException extends ApiException {
  public IssueFailedException(String msgKey, Object... params) {
    super("key = " + msgKey + "; params = " + Arrays.toString(params));
    this.msgKey = msgKey;
    this.msgParams = params;
  }
}
