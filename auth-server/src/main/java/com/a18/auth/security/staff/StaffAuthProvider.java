package com.a18.auth.security.staff;

import com.a18.auth.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.cache.NullUserCache;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Component
@Lazy
public class StaffAuthProvider implements AuthenticationProvider {

  private static final CharSequence STAFF_NOT_FOUND_PASSWORD = "staffNotFoundPassword";

  @Autowired @Lazy private StaffService staffService;

  @Autowired @Lazy private PasswordEncoder passwordEncoder;

  @Autowired @Lazy private MessageSource messageSource;

  /**
   * The password used to perform {@link PasswordEncoder#matches(CharSequence, String)} on when the
   * user is not found to avoid SEC-2056. This is necessary, because some {@link PasswordEncoder}
   * implementations will short circuit if the password is not in a valid format.
   */
  private volatile String userNotFoundEncodedPassword;

  private UserCache userCache = new NullUserCache();

  private AccountStatusUserDetailsChecker checker = new AccountStatusUserDetailsChecker();

  private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

  @Override public boolean supports(Class<?> authentication) {
    return StaffAuthenticationToken.class.isAssignableFrom(authentication);
  }

  @Override @Transactional public Authentication authenticate(Authentication authentication)
      throws AuthenticationException {
    Assert.isInstanceOf(StaffAuthenticationToken.class, authentication);
    String username = String.valueOf(authentication.getPrincipal());
    boolean cacheWasUsed = true;
    UserDetails user = this.userCache.getUserFromCache(username);

    if (user == null) {
      user = this.retrieveUser(username, (StaffAuthenticationToken) authentication);
      cacheWasUsed = false;
    }

    Assert.notNull(user, "retrieveUser returned null - a violation of the interface contract");

    try {
      this.checker.check(user);
    } catch (AuthenticationException exception) {
      if (cacheWasUsed) {
        // There was a problem, so try again after checking we're using latest data (i.e. not from the cache)
        cacheWasUsed = false;
        user = retrieveUser(username, (StaffAuthenticationToken) authentication);
        if (user != null) { this.checker.check(user); }
      } else {
        throw exception;
      }
    }

    if (!cacheWasUsed) {
      this.userCache.putUserInCache(user);
    }

    StaffAuthenticationToken result = new StaffAuthenticationToken(
        user,
        user.getPassword(),
        this.authoritiesMapper.mapAuthorities(user.getAuthorities())
    );
    result.setDetails(authentication.getDetails());

    return result;
  }

  private UserDetails retrieveUser(String username, StaffAuthenticationToken authentication) {
    this.prepareTimingAttackProtection();
    try {
      UserDetails loadedUser = this.staffService.loadUserByUsername(username);
      if (loadedUser == null
          || !this.passwordEncoder.matches(
          String.valueOf(authentication.getCredentials()),
          loadedUser.getPassword()
      )) {
        throw new InternalAuthenticationServiceException(this.messageSource.getMessage(
            "username.password.not.found",
            new Object[] {},
            LocaleContextHolder.getLocale()
        ));
      }
      return loadedUser;
    } catch (UsernameNotFoundException ex) {
      mitigateAgainstTimingAttack(authentication);
      throw ex;
    } catch (InternalAuthenticationServiceException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new InternalAuthenticationServiceException(ex.getMessage(), ex);
    }
  }

  private void prepareTimingAttackProtection() {
    if (this.userNotFoundEncodedPassword == null) {
      this.userNotFoundEncodedPassword = this.passwordEncoder.encode(STAFF_NOT_FOUND_PASSWORD);
    }
  }

  private void mitigateAgainstTimingAttack(StaffAuthenticationToken authentication) {
    if (authentication.getCredentials() != null) {
      String presentedPassword = authentication.getCredentials().toString();
      this.passwordEncoder.matches(presentedPassword, this.userNotFoundEncodedPassword);
    }
  }
}
