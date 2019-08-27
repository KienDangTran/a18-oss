package com.a18.common.config;

import java.security.KeyPair;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.AccessTokenProviderChain;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.security.oauth2.common.AuthenticationScheme;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CommonSecurityConfig {
  @Value("${server.ssl.key-store}")
  private String keystore;

  @Value("${server.ssl.key-store-password}")
  private char[] keystorePassword;

  @Value("${server.ssl.key-alias}")
  private String sslKeyAlias;

  @Value("${security.oauth2.client.access-token-uri}")
  private String accessTokenUri;

  @Value("${security.oauth2.client.client-id}")
  private String clientId;

  @Value("${security.oauth2.client.client-secret}")
  private String secret;

  @Value("${security.oauth2.client.authentication-scheme}")
  private AuthenticationScheme authenticationScheme;

  @Value("${security.oauth2.client.grant-type}")
  private String grantType;

  @Value("${security.oauth2.client.token-name}")
  private String tokenName;

  @Autowired private OAuth2ClientContext oauth2ClientContext;

  @Bean
  public TokenStore tokenStore() {
    return new JwtTokenStore(this.accessTokenConverter());
  }

  @Bean
  @Primary
  public JwtAccessTokenConverter accessTokenConverter() {
    JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
    converter.setAccessTokenConverter(new JwtConverter());
    Resource keystoreResource = this.keystore.startsWith("classpath:")
                                ? new ClassPathResource(this.keystore)
                                : new FileSystemResource(this.keystore);
    KeyStoreKeyFactory keyStoreKeyFactory =
        new KeyStoreKeyFactory(keystoreResource, this.keystorePassword);
    KeyPair keyPair = keyStoreKeyFactory.getKeyPair(this.sslKeyAlias, this.keystorePassword);
    converter.setKeyPair(keyPair);
    return converter;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @Bean
  ResourceOwnerPasswordResourceDetails resourceOwnerPasswordResourceDetails() {
    ResourceOwnerPasswordResourceDetails resource = new ResourceOwnerPasswordResourceDetails();
    resource.setAccessTokenUri(this.accessTokenUri);
    resource.setClientAuthenticationScheme(this.authenticationScheme);
    resource.setClientId(this.clientId);
    resource.setClientSecret(this.secret);
    resource.setGrantType(this.grantType);
    resource.setTokenName(this.tokenName);
    return resource;
  }

  @Bean
  OAuth2RestTemplate oAuth2RestTemplate() {
    OAuth2RestTemplate template = new OAuth2RestTemplate(
        this.resourceOwnerPasswordResourceDetails(),
        this.oauth2ClientContext
    );
    AccessTokenProviderChain provider = new AccessTokenProviderChain(
        Collections.singletonList(new ResourceOwnerPasswordAccessTokenProvider())
    );
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setOutputStreaming(false);

    template.setAccessTokenProvider(provider);
    template.setRequestFactory(requestFactory);

    return template;
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of(CorsConfiguration.ALL));
    configuration.setAllowedMethods(List.of(CorsConfiguration.ALL));
    configuration.setAllowedHeaders(List.of(CorsConfiguration.ALL));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
