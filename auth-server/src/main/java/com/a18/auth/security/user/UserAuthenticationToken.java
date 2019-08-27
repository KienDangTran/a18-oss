package com.a18.auth.security.user;

import java.util.Collection;
import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class UserAuthenticationToken extends UsernamePasswordAuthenticationToken {
  @Getter
  private final String deviceRegistrationToken;

  public UserAuthenticationToken(
      Object principal,
      Object credentials,
      Collection<? extends GrantedAuthority> authorities,
      String deviceRegistrationToken
  ) {
    super(principal, credentials, authorities);
    this.deviceRegistrationToken = deviceRegistrationToken;
  }
}
