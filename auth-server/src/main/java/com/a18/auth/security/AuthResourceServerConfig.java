package com.a18.auth.security;

import com.a18.auth.controller.StaffController;
import com.a18.auth.controller.UserController;
import com.a18.auth.security.staff.StaffLoginFilter;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableResourceServer
public class AuthResourceServerConfig extends ResourceServerConfigurerAdapter {

  @Autowired @Lazy private DefaultTokenServices tokenServices;

  @Autowired @Lazy private AuthenticationManager authenticationManager;

  @Autowired @Lazy private ClientDetailsService clientDetails;

  @Autowired @Lazy private ResourceOwnerPasswordResourceDetails
      resourceOwnerPasswordResourceDetails;

  @Value("${security.oauth2.client.resource-ids}")
  private String resourcesId;

  @Override
  public void configure(ResourceServerSecurityConfigurer resources) {
    resources.resourceId(resourcesId);
  }

  @Override
  public void configure(HttpSecurity http) throws Exception {
    http
        .csrf().disable()
        .cors()
        .and()
        .formLogin().disable()
        .httpBasic().disable()
        .authorizeRequests()
        .antMatchers(HttpMethod.POST, UserController.PATH_USER).permitAll()
        .antMatchers(
            StaffController.PATH_STAFF_LOGIN,
            UserController.PATH_USER_LOGIN,
            UserController.PATH_USER + "/search/countAllByUsername",
            UserController.PATH_USER + "/search/countAllByEmail",
            UserController.PATH_USER + "/search/countAllByPhone",
            "/actuator/health"
        ).permitAll()
        .anyRequest().fullyAuthenticated()
        .and()
        .exceptionHandling()
        .authenticationEntryPoint((request, response, authException) ->
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
        .accessDeniedHandler((request, response, authException) ->
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED));

    http.addFilterAfter(
        new StaffLoginFilter(this.authenticationManager, StaffController.PATH_STAFF + "/**"),
        BasicAuthenticationFilter.class
    );
  }

  @Bean
  OwnerPasswordTokenGranter ownerPasswordTokenGranter() {
    return new OwnerPasswordTokenGranter(
        this.tokenServices,
        this.clientDetails,
        new DefaultOAuth2RequestFactory(clientDetails),
        this.authenticationManager,
        this.resourceOwnerPasswordResourceDetails
    );
  }
}
