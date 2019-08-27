package com.a18.common.util;

import com.a18.common.dto.UserDTO;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Lazy
public class AuthUtil {

  @Autowired @Lazy private OAuth2RestTemplate restTemplate;

  @Value("${auth-server-uri}")
  private String authServerUri;

  @Autowired @Lazy private TokenStore tokenStore;

  public Optional<UserDTO> retrieveUserInfoByUsername(String username) {
    if (StringUtils.isBlank(username)) return Optional.empty();

    RequestEntity request = RequestEntity
        .get(URI.create(this.authServerUri + "/users/search/findByUsername?username=" + username))
        .header(HttpHeaders.CACHE_CONTROL, "no-cache")
        .build();

    UserDTO userDTO = this.restTemplate.exchange(request, UserDTO.class).getBody();

    return Optional.ofNullable(userDTO);
  }

  public String getUserDeviceRegistrationToken() {
    try {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      OAuth2AuthenticationDetails details = ((OAuth2AuthenticationDetails) auth.getDetails());
      OAuth2AccessToken token = this.tokenStore.readAccessToken(details.getTokenValue());
      Map<String, Object> additionalInfo = token.getAdditionalInformation();
      return Objects.toString(additionalInfo.get("deviceRegistrationToken"), "");
    } catch (NullPointerException e) {
      return "";
    }
  }
}
