package com.a18.auth.controller;

import com.a18.auth.security.OwnerPasswordTokenGranter;
import com.a18.auth.service.StaffService;
import com.a18.common.exception.ApiError;
import java.security.Principal;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.PostMapping;

@RepositoryRestController
public class StaffController {
  public static final String PATH_STAFF = "/staffs";

  public static final String PATH_STAFF_LOGIN = PATH_STAFF + "/login";

  private static final String PATH_STAFF_LOGOUT_ALL = PATH_STAFF + "/logoutAll";

  private static final String PATH_STAFF_LOGOUT = PATH_STAFF + "/logout";

  private static final String PATH_STAFF_CHANGE_PASSWORD = PATH_STAFF + "/changePassword";

  private final OwnerPasswordTokenGranter tokenGranter;

  private final StaffService staffService;

  @Autowired public StaffController(
      OwnerPasswordTokenGranter tokenGranter,
      StaffService staffService
  ) {
    this.tokenGranter = tokenGranter;
    this.staffService = staffService;
  }

  @PostMapping(value = PATH_STAFF_LOGIN, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ResponseEntity login(Authentication authentication) {
    OAuth2AccessToken token = this.tokenGranter.grant(authentication);

    return token == null
           ? ResponseEntity.badRequest()
                           .body(new ApiError(
                               HttpStatus.BAD_REQUEST,
                               "username.password.not.found"
                           ))
           : ResponseEntity.ok().body(token);
  }

  @PostMapping(PATH_STAFF_LOGOUT_ALL)
  public ResponseEntity logoutAllSession(Principal principal) {
    this.staffService.logoutAllSession(principal.getName());
    return ResponseEntity.noContent().build();
  }

  @PostMapping(PATH_STAFF_LOGOUT)
  public ResponseEntity logoutCurrentSession(Authentication auth) {
    this.staffService.logoutCurrentSession(auth);
    return ResponseEntity.noContent().build();
  }

  @PostMapping(PATH_STAFF_CHANGE_PASSWORD)
  public ResponseEntity changePassword(HttpServletRequest request, Principal principal)
      throws MissingServletRequestParameterException {
    this.staffService.changePassword(request, principal.getName());
    return ResponseEntity.ok().build();
  }
}
