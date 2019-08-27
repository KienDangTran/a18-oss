package com.a18.auth.security.user;

import com.a18.auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class UserAuthProvider extends DaoAuthenticationProvider {

  @Autowired @Lazy private UserService userService;

  @Override protected void doAfterPropertiesSet() throws Exception {
    super.setUserDetailsService(this.userService);
    super.setHideUserNotFoundExceptions(false);
    super.doAfterPropertiesSet();
  }

  @Override public boolean supports(Class<?> authentication) {
    return super.supports(authentication);
  }

  @Override protected Authentication createSuccessAuthentication(
      Object principal,
      Authentication authentication,
      UserDetails user
  ) {
    if (authentication instanceof UserAuthenticationToken) {
      return new UserAuthenticationToken(
          principal,
          authentication.getCredentials(),
          authentication.getAuthorities(),
          ((UserAuthenticationToken) authentication).getDeviceRegistrationToken()
      );
    }

    return super.createSuccessAuthentication(principal, authentication, user);
  }
}