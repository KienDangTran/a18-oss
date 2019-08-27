package com.a18.auth.security;

import com.a18.common.constant.GrantType;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

public class OwnerPasswordTokenGranter extends AbstractTokenGranter {

  public static final String USERNAME_PARAM_NAME = "username";

  public static final String PASSWORD_PARAM_NAME = "password";

  private AuthenticationManager authenticationManager;

  private OAuth2ProtectedResourceDetails resourceDetails;

  private ClientDetailsService clientDetailsService;

  private Authentication authentication;

  protected OwnerPasswordTokenGranter(
      AuthorizationServerTokenServices tokenServices,
      ClientDetailsService clientDetailsService,
      OAuth2RequestFactory requestFactory,
      AuthenticationManager authenticationManager,
      OAuth2ProtectedResourceDetails resourceDetails
  ) {
    super(tokenServices, clientDetailsService, requestFactory, GrantType.PASSWORD);
    this.authenticationManager = authenticationManager;
    this.resourceDetails = resourceDetails;
    this.clientDetailsService = clientDetailsService;
  }

  @Override
  protected OAuth2Authentication getOAuth2Authentication(
      ClientDetails client,
      TokenRequest tokenRequest
  ) {

    Map<String, String> parameters = new LinkedHashMap<>(tokenRequest.getRequestParameters());
    String username = parameters.get(USERNAME_PARAM_NAME);
    String password = parameters.get(PASSWORD_PARAM_NAME);
    // Protect from downstream leaks of password
    parameters.remove(PASSWORD_PARAM_NAME);

    if (this.authentication == null) {
      this.authentication = new UsernamePasswordAuthenticationToken(username, password);
      ((AbstractAuthenticationToken) authentication).setDetails(parameters);
    }

    try {
      this.authentication = this.authenticationManager.authenticate(this.authentication);
    } catch (AccountStatusException ase) {
      //covers expired, locked, disabled cases (mentioned in section 5.2, draft 31)
      throw new InvalidGrantException("user.info.lock");
    } catch (UsernameNotFoundException ex) {
      throw new InvalidGrantException("username.not.found");
    } catch (BadCredentialsException ex) {
      throw new InvalidGrantException("username.password.not.found");
    }

    if (this.authentication == null || !this.authentication.isAuthenticated()) {
      throw new InvalidGrantException("Could not authenticate user: " + username);
    }

    OAuth2Request storedOAuth2Request =
        getRequestFactory().createOAuth2Request(client, tokenRequest);

    return new OAuth2Authentication(storedOAuth2Request, this.authentication);
  }

  public OAuth2AccessToken grant(Authentication authentication) {
    ClientDetails authenticatedClient =
        this.clientDetailsService.loadClientByClientId(this.resourceDetails.getClientId());
    Map<String, String> parameters = Map.of(
        OAuth2Utils.CLIENT_ID, this.resourceDetails.getClientId(),
        OAuth2Utils.GRANT_TYPE, this.resourceDetails.getGrantType(),
        USERNAME_PARAM_NAME, String.valueOf(authentication.getPrincipal()),
        PASSWORD_PARAM_NAME, String.valueOf(authentication.getCredentials())
    );

    this.authentication = authentication;
    TokenRequest tokenRequest =
        this.getRequestFactory().createTokenRequest(parameters, authenticatedClient);

    return super.grant(GrantType.PASSWORD, tokenRequest);
  }

  @Override public AuthorizationServerTokenServices getTokenServices() {
    return super.getTokenServices();
  }
}
