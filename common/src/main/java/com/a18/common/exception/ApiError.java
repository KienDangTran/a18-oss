package com.a18.common.exception;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import org.springframework.http.HttpStatus;

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonTypeIdResolver(LowerCamelCaseClassNameTypeResolver.class)
public class ApiError {

  private static final String DEFAULT_MESSAGE = "Unexpected error";

  private final List<ApiSubError> errors = new ArrayList<>();

  public HttpStatus status;

  public String message;

  public String debugMessage;

  public ApiError(@NonNull HttpStatus status, @NonNull String message, Throwable ex) {
    this.status = status;
    this.message = message;
    this.debugMessage = extractDebugMessage(ex);
  }

  public ApiError(HttpStatus status, Throwable ex) {
    this(status, DEFAULT_MESSAGE, ex);
  }

  public ApiError(HttpStatus status, String message) {
    this(status, message, null);
  }

  private String extractDebugMessage(Throwable ex) {
    if (ex == null) {
      return "";
    }
    return ex.getLocalizedMessage();
  }

  public void addErrors(@NonNull Collection<ApiSubError> errors) {
    this.errors.addAll(errors);
  }

  public void addError(@NonNull ApiSubError subError) {
    this.errors.add(subError);
  }

  /**
   * @return unmodifiable view of the {@link #errors}.
   */
  public List<ApiSubError> getErrors() {
    return Collections.unmodifiableList(errors);
  }

  /**
   * Validation error.
   *
   * To understand how to use and build messages which this class. Take the following json
   * serialized example:
   *
   * <pre>
   * {
   *   "field": "loginName",
   *   "rejectValue" : "test",
   *   "message": "userInfoes.loginName.existed",
   *   "debugMessage": "loginName 'test' is already existed"
   * }
   * </pre>
   */
  @Builder
  public static class ApiSubError {

    /**
     * Field or object property that cause validation error.
     */
    public String field;

    /**
     * The value that cause validation error.
     */
    public String rejectedValue;

    /**
     * The message to be shown on the frontend, which actually message key only. Frontend will use
     * this key so search for the translated text.
     */
    public String message;

    /**
     * Debug message which will provide useful information for developer to troubleshoot.
     */
    public String debugMessage;
  }
}
