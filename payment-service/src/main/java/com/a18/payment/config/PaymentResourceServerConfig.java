package com.a18.payment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;

@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class PaymentResourceServerConfig extends ResourceServerConfigurerAdapter {
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
        .antMatchers("/actuator/health").permitAll()
        .anyRequest().authenticated();
  }
}
