package com.a18.auth.controller;

import com.a18.auth.model.User;
import com.a18.auth.security.OwnerPasswordTokenGranter;
import com.a18.auth.security.user.UserAuthenticationToken;
import com.a18.auth.service.UserService;
import com.a18.auth.specification.UserSpecification;
import com.a18.common.constant.Privilege;
import com.a18.common.exception.ApiError;
import com.a18.common.util.ResourceAssemblerHelper;
import java.security.Principal;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RepositoryRestController
public class UserController {

  public static final String PATH_USER = "/users";

  public static final String PATH_USER_LOGIN = PATH_USER + "/login";

  private static final String PATH_USER_LOGOUT_ALL = PATH_USER + "/logoutAll";

  private static final String PATH_USER_LOGOUT = PATH_USER + "/logout";

  private static final String PATH_USER_CHANGE_PASSWORD = PATH_USER + "/changePassword";

  private final UserService userService;

  private final OwnerPasswordTokenGranter tokenGranter;

  private final ResourceAssemblerHelper resourceAssemblerHelper;

  private final PagedResourcesAssembler<User> pagedAssembler;

  @Autowired @Lazy public UserController(
      UserService userService,
      OwnerPasswordTokenGranter tokenGranter,
      ResourceAssemblerHelper resourceAssemblerHelper,
      PagedResourcesAssembler<User> pagedAssembler
  ) {
    this.userService = userService;
    this.tokenGranter = tokenGranter;
    this.resourceAssemblerHelper = resourceAssemblerHelper;
    this.pagedAssembler = pagedAssembler;
  }

  @PostMapping(path = PATH_USER_LOGIN, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ResponseEntity login(HttpServletRequest request)
      throws MissingServletRequestParameterException {
    String username = request.getParameter(OwnerPasswordTokenGranter.USERNAME_PARAM_NAME);
    String password = request.getParameter(OwnerPasswordTokenGranter.PASSWORD_PARAM_NAME);
    String registrationTokens = request.getParameter("registrationTokens");

    if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
      throw new MissingServletRequestParameterException(
          OwnerPasswordTokenGranter.USERNAME_PARAM_NAME
              + "/"
              + OwnerPasswordTokenGranter.PASSWORD_PARAM_NAME,
          String.class.getSimpleName()
      );
    }

    Authentication auth = new UserAuthenticationToken(
        username,
        password,
        List.of(),
        registrationTokens
    );

    SecurityContextHolder.getContext().setAuthentication(auth);
    OAuth2AccessToken token = this.tokenGranter.grant(auth);

    return token == null
           ? ResponseEntity
               .badRequest()
               .body(new ApiError(HttpStatus.BAD_REQUEST, "username.password.not.found"))
           : ResponseEntity.ok().body(token);
  }

  @PostMapping(PATH_USER_LOGOUT_ALL)
  public ResponseEntity logoutAllSession(Principal principal) {
    this.userService.logoutAllSession(principal.getName());
    return ResponseEntity.noContent().build();
  }

  @PostMapping(PATH_USER_LOGOUT)
  public ResponseEntity logoutCurrentSession(Authentication auth) {
    this.userService.logoutCurrentSession(auth);
    return ResponseEntity.noContent().build();
  }

  @PostMapping(PATH_USER_CHANGE_PASSWORD)
  public ResponseEntity changePassword(HttpServletRequest request, Principal principal)
      throws MissingServletRequestParameterException {
    this.userService.changePassword(request, principal.getName());
    return ResponseEntity.ok().build();
  }

  @PreAuthorize("hasAuthority('" + Privilege.READ + Privilege.USER + "')"
                    + " or #username.equals(authentication.name)")
  @GetMapping(PATH_USER)
  public ResponseEntity findBySpec(
      @RequestParam(required = false) String username,
      @RequestParam(required = false) String fullname,
      @RequestParam(required = false) String email,
      @RequestParam(required = false) String phone,
      @RequestParam(required = false) Boolean enabled,
      @PageableDefault Pageable pageable
  ) {
    return ResponseEntity.ok(this.pagedAssembler.toResource(
        this.userService.findBySpec(
            UserSpecification.filterUser(username, fullname, email, phone, enabled),
            pageable
        ),
        resourceAssemblerHelper.resourceAssembler()
    ));
  }

  @GetMapping(PATH_USER + "/currentUser")
  public ResponseEntity getCurrentUser(Authentication authentication) {
    if (authentication != null && StringUtils.isNotEmpty(authentication.getName())) {
      return ResponseEntity.ok(
          this.userService.loadUserByUsername(StringUtils.trimToNull(authentication.getName()))
      );
    }

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  }
}
