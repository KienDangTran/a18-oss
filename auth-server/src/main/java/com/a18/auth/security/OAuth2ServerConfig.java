package com.a18.auth.security;

import com.a18.auth.model.AbstractUserDetails;
import com.a18.auth.service.TokenBlackListService;
import com.a18.common.constant.GrantType;
import com.a18.common.constant.PreDefinedRole;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

/**
 * responsible for generating tokens specific to a client
 * <pre>
 * grant types:
 *  - "authorization_code", // used by web server apps(server-to-server communication)
 *  - "client_credentials", // used by the client themselves to get an access token
 *  - "refresh_token",
 *  - "password", // used with trusted Applications, such as those owned by the service itself
 *  - "implicit" // used in browser based application (that run on the user's device)
 * </pre>
 */
@Configuration
@EnableAuthorizationServer
public class OAuth2ServerConfig extends AuthorizationServerConfigurerAdapter {

  @Value("${security.oauth2.client.client-secret}")
  private String clientSecret;

  @Value("${security.oauth2.client.client-id}")
  private String clientId;

  @Value("${security.oauth2.client.access-token-validity-seconds}")
  private int accessTokenValiditySeconds;

  @Value("${security.oauth2.client.refresh-token-validity-seconds}")
  private int refreshTokenValiditySeconds;

  @Autowired private AuthenticationManager authenticationManager;

  @Autowired private TokenBlackListService blackListService;

  @Autowired private TokenStore tokenStore;

  @Autowired private JwtAccessTokenConverter accessTokenConverter;

  @Autowired private PasswordEncoder passwordEncoder;

  @Override
  public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
    endpoints
        .authenticationManager(this.authenticationManager)
        .tokenServices(this.tokenServices())
        .tokenStore(this.tokenStore)
        .accessTokenConverter(this.accessTokenConverter);
  }

  @Override
  public void configure(AuthorizationServerSecurityConfigurer oauthServer) {
    oauthServer
        // we're allowing access to the token only for clients with 'TRUSTED_CLIENT' authority
        .tokenKeyAccess("hasRole('" + PreDefinedRole.ROLE_TRUSTED_CLIENT + "')")
        .checkTokenAccess("hasRole('" + PreDefinedRole.ROLE_TRUSTED_CLIENT + "')");
  }

  @Override
  public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
    clients
        .inMemory()
        .withClient(this.clientId)
        .authorizedGrantTypes(GrantType.PASSWORD)
        .authorities(PreDefinedRole.ROLE_TRUSTED_CLIENT)
        .scopes(PreDefinedRole.ROLE_TRUSTED_CLIENT)
        .secret(this.passwordEncoder.encode(clientSecret))
        .autoApprove(true)
        .accessTokenValiditySeconds(accessTokenValiditySeconds)
        .refreshTokenValiditySeconds(refreshTokenValiditySeconds);
  }

  @Bean
  @Primary
  public DefaultTokenServices tokenServices() {
    DefaultTokenServices tokenService = new CustomerTokenService();
    tokenService.setTokenStore(this.tokenStore);
    tokenService.setTokenEnhancer(this.accessTokenConverter);
    return tokenService;
  }

  class CustomerTokenService extends DefaultTokenServices {

    @Override
    public OAuth2AccessToken createAccessToken(OAuth2Authentication authentication) {
      DefaultOAuth2AccessToken token =
          new DefaultOAuth2AccessToken(super.createAccessToken(authentication));
      AbstractUserDetails user = (AbstractUserDetails) authentication.getPrincipal();
      if (!user.getAuthorities().isEmpty()) {
        token.setScope(user.getAuthorities()
                           .stream()
                           .map(GrantedAuthority::getAuthority)
                           .collect(Collectors.toSet()));
      }

      String jti = (String) token.getAdditionalInformation().get("jti");
      blackListService.addToEnabledList(user.getUsername(), jti, token.getExpiration().getTime());

      return token;
    }

    @Override
    public OAuth2AccessToken refreshAccessToken(String refreshTokenValue, TokenRequest tokenRequest)
        throws AuthenticationException {
      String jti = tokenRequest.getRequestParameters().get("jti");
      if (StringUtils.isBlank(jti)
          || !blackListService.isTokenExists(jti)
          || blackListService.isBlacklisted(jti)) { return null; }
      blackListService.addToBlackList(jti);

      return super.refreshAccessToken(refreshTokenValue, tokenRequest);
    }

    @Override public OAuth2Authentication loadAuthentication(String accessTokenValue)
        throws AuthenticationException, InvalidTokenException {
      OAuth2AccessToken token = this.readAccessToken(accessTokenValue);

      String jti = (String) token.getAdditionalInformation().get("jti");
      if (StringUtils.isBlank(jti)
          || !blackListService.isTokenExists(jti)
          || blackListService.isBlacklisted(jti)) {
        throw new InvalidTokenException("Token is not exists or was blacklisted");
      }

      return super.loadAuthentication(accessTokenValue);
    }
  }
}
