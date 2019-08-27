package com.a18.common.exception;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.StaleObjectStateException;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.DataException;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.rest.core.RepositoryConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.client.resource.OAuth2AccessDeniedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
public class CommonExceptionHandler
    extends ResponseEntityExceptionHandler
    implements AsyncUncaughtExceptionHandler {

  @Autowired @Lazy protected MessageSource messageSource;

  @InitBinder
  public void initBinder(WebDataBinder binder) {
    StringTrimmerEditor stringTrimmer = new StringTrimmerEditor(true);
    binder.registerCustomEditor(String.class, stringTrimmer);
  }

  /**
   * Happens when request JSON is malformed.
   */
  @Override protected ResponseEntity<Object> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request
  ) {
    return buildResponseEntity(request, status, ex.getLocalizedMessage());
  }

  @Override protected ResponseEntity<Object> handleMissingServletRequestParameter(
      MissingServletRequestParameterException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request
  ) {
    return buildResponseEntity(request, status, ex.getLocalizedMessage());
  }

  @ExceptionHandler(OAuth2AccessDeniedException.class)
  public ResponseEntity handleOAuth2AccessDeniedException(
      WebRequest request,
      OAuth2AccessDeniedException ex
  ) {
    return this.buildResponseEntity(request, HttpStatus.FORBIDDEN, ex.getLocalizedMessage());
  }

  @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
  public ResponseEntity handleObjectOptimisticLockingFailureException(
      WebRequest request,
      ObjectOptimisticLockingFailureException ex
  ) {
    return this.buildResponseEntity(
        request,
        HttpStatus.INTERNAL_SERVER_ERROR,
        ex.getLocalizedMessage()
    );
  }

  @ExceptionHandler(StaleObjectStateException.class)
  public ResponseEntity handleStaleObjectStateException(
      WebRequest request,
      StaleObjectStateException ex
  ) {
    return this.buildResponseEntity(
        request,
        HttpStatus.INTERNAL_SERVER_ERROR,
        ex.getLocalizedMessage()
    );
  }

  @ExceptionHandler(RepositoryConstraintViolationException.class)
  protected ResponseEntity handleRepositoryConstraintViolationException(
      RepositoryConstraintViolationException ex,
      WebRequest request
  ) {
    ApiError apierror = new ApiError(HttpStatus.CONFLICT, ex.getLocalizedMessage());
    if (ex.getErrors().getFieldErrors().isEmpty()) {
      apierror.addErrors(
          ex.getErrors()
            .getAllErrors()
            .stream()
            .map(
                error -> ApiError.ApiSubError
                    .builder()
                    .message(this.messageSource.getMessage(
                        StringUtils.trimToEmpty(error.getCode()),
                        this.localizeParams(error.getArguments(), request.getLocale()),
                        request.getLocale()
                    ))
                    .debugMessage(error.getCode())
                    .build()
            )
            .collect(Collectors.toList())
      );
    } else {
      apierror.addErrors(ex.getErrors().getFieldErrors().stream().map(
          fieldError -> ApiError.ApiSubError
              .builder()
              .field(this.messageSource.getMessage(
                  fieldError.getField(),
                  null,
                  request.getLocale()
              ))
              .rejectedValue(fieldError.getRejectedValue() == null
                             ? ""
                             : String.valueOf(fieldError.getRejectedValue()))
              .message(this.messageSource.getMessage(
                  StringUtils.trimToEmpty(fieldError.getCode()),
                  this.localizeParams(fieldError.getArguments(), request.getLocale()),
                  request.getLocale()
              ))
              .debugMessage(fieldError.getCode())
              .build()
          ).collect(Collectors.toList())
      );
    }

    return this.buildResponseEntity(apierror);
  }

  @ExceptionHandler(AccessDeniedException.class)
  protected ResponseEntity handleAccessDeniedException(
      AccessDeniedException ex,
      WebRequest request
  ) {
    return buildResponseEntity(request, HttpStatus.FORBIDDEN, ex.getLocalizedMessage());
  }

  /**
   * Handle EntityNotFoundException
   */
  @ExceptionHandler(EntityNotFoundException.class)
  protected ResponseEntity handleEntityNotFound(EntityNotFoundException ex, WebRequest request) {
    return buildResponseEntity(request, HttpStatus.NOT_FOUND, ex.getLocalizedMessage());
  }

  /**
   * Handle DataIntegrityViolationException, inspects the cause for different DB causes.
   *
   * @param ex the DataIntegrityViolationException
   * @return the ApiError object
   */
  @ExceptionHandler(DataIntegrityViolationException.class)
  protected ResponseEntity handleDataIntegrityViolation(
      DataIntegrityViolationException ex,
      WebRequest request
  ) {
    if (ex.getCause() instanceof ConstraintViolationException) {
      return buildResponseEntity(
          request,
          HttpStatus.CONFLICT,
          ex.getCause().getCause().getLocalizedMessage()
      );
    }
    if (ex.getCause() instanceof DataException) {
      return buildResponseEntity(
          request,
          HttpStatus.CONFLICT,
          ex.getCause().getCause().getLocalizedMessage()
      );
    }

    return buildResponseEntity(request, HttpStatus.CONFLICT, ex.getLocalizedMessage());
  }

  @ExceptionHandler(HttpServerErrorException.class)
  protected ResponseEntity handleHttpServerErrorException(
      HttpServerErrorException ex,
      WebRequest request
  ) {
    return buildResponseEntity(request, ex.getStatusCode(), ex.getResponseBodyAsString());
  }

  @ExceptionHandler(HttpClientErrorException.class)
  protected ResponseEntity handleHttpClientErrorException(
      HttpClientErrorException ex,
      WebRequest request
  ) {
    return buildResponseEntity(request, ex.getStatusCode(), ex.getResponseBodyAsString());
  }

  @ExceptionHandler(NoSuchElementException.class)
  protected ResponseEntity handleNoSuchElementException(
      NoSuchElementException ex,
      WebRequest request
  ) {
    return this.buildResponseEntity(request, HttpStatus.NOT_FOUND, ex.getLocalizedMessage());
  }

  @ExceptionHandler(RestClientException.class)
  protected ResponseEntity handleRestClientException(RestClientException ex, WebRequest request) {
    return this.buildResponseEntity(
        request,
        HttpStatus.INTERNAL_SERVER_ERROR,
        ex.getLocalizedMessage()
    );
  }

  @ExceptionHandler(ApiException.class)
  private ResponseEntity handleApiException(ApiException ex, WebRequest request) {
    return this.buildResponseEntity(
        request,
        HttpStatus.BAD_REQUEST,
        ex.msgKey,
        ex.msgParams
    );
  }

  @ExceptionHandler(Throwable.class)
  protected ResponseEntity handleGenericExcetion(Throwable ex, WebRequest request) {
    if (ex.getCause() != null && ex.getCause().getClass().equals(ApiException.class)) {
      return this.handleApiException((ApiException) ex.getCause(), request);
    }
    return this.buildResponseEntity(
        request,
        HttpStatus.INTERNAL_SERVER_ERROR,
        ex.getLocalizedMessage()
    );
  }

  protected ResponseEntity<Object> buildResponseEntity(
      WebRequest request,
      HttpStatus httpStatus,
      String msgKey,
      Object... msgParams
  ) {
    ApiError apiError = new ApiError(
        httpStatus == null ? HttpStatus.INTERNAL_SERVER_ERROR : httpStatus,
        this.messageSource.getMessage(
            StringUtils.trimToEmpty(msgKey),
            msgParams == null ? new Object[] {} : msgParams,
            LocaleContextHolder.getLocale()
        )
    );

    log.error("{}; {}", apiError.message, request);
    return this.buildResponseEntity(apiError);
  }

  private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
    return ResponseEntity.status(apiError.status).body(apiError);
  }

  private Object[] localizeParams(Object[] params, Locale locale) {
    if (params == null || params.length == 0) return new Object[] {""};
    return Arrays.stream(params)
                 .map(param -> this.messageSource.getMessage(String.valueOf(param), null, locale))
                 .toArray();
  }

  @Override public void handleUncaughtException(Throwable ex, Method method, Object... params) {
    throw new RuntimeException(ex);
  }
}
