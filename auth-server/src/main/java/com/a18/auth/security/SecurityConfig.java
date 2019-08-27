package com.a18.auth.security;

import com.a18.auth.security.staff.StaffAuthProvider;
import com.a18.auth.security.user.UserAuthProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired @Lazy private StaffAuthProvider staffAuthProvider;

  @Autowired @Lazy private UserAuthProvider userAuthProvider;

  @Override protected void configure(AuthenticationManagerBuilder authBuilder) {
    authBuilder.authenticationProvider(this.staffAuthProvider)
               .authenticationProvider(this.userAuthProvider);
  }

  @Bean
  @Override protected AuthenticationManager authenticationManager() throws Exception {
    return super.authenticationManager();
  }
}