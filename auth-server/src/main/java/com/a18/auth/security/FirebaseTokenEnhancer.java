package com.a18.auth.security;

import com.a18.auth.model.User;
import com.a18.auth.security.user.UserAuthenticationToken;
import com.a18.common.firebase.FirebaseConfig;
import com.a18.common.firebase.FirebaseCredentials;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class FirebaseTokenEnhancer extends JwtAccessTokenConverter {

  @Autowired @Lazy private FirebaseConfig firebaseConfig;

  @Value("${security.oauth2.client.resource-ids}")
  private List<String> resourceIds;

  @Value("${security.oauth2.client.scope}")
  private List<String> scope;

  @Override
  public OAuth2AccessToken enhance(
      OAuth2AccessToken accessToken,
      OAuth2Authentication authentication
  ) {
    if (authentication.getPrincipal() instanceof User) {
      DefaultOAuth2AccessToken result = new DefaultOAuth2AccessToken(accessToken);
      User user = (User) authentication.getPrincipal();
      FirebaseCredentials firebaseCredentials = this.firebaseConfig.getFirebaseCredentials();
      long now = LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond();
      result.setAdditionalInformation(
          Map.of(
              "iss", firebaseCredentials.getClient_email(),
              "sub", firebaseCredentials.getClient_email(),
              AccessTokenConverter.AUD, resourceIds.isEmpty() ? "" : resourceIds.get(0),
              AccessTokenConverter.EXP, now + 3600,
              "iat", now,
              AccessTokenConverter.SCOPE, scope.isEmpty() ? "" : scope.get(0),
              "uid", Objects.toString(user.getFirebaseUid(), ""),
              TOKEN_ID, result.getValue(),
              "deviceRegistrationToken", Objects.toString(((UserAuthenticationToken) authentication
                  .getUserAuthentication()).getDeviceRegistrationToken(), "")
          )
      );
      result.setValue(encode(result, authentication));
      return result;
    }

    return super.enhance(accessToken, authentication);
  }
}