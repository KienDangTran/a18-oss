package com.a18.account.event;

import com.a18.account.model.Account;
import com.a18.account.model.repository.BalanceRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler
@Lazy
public class AccountRepositoryEventHandler {

  @Autowired @Lazy PasswordEncoder passwordEncoder;

  @Autowired @Lazy private BalanceRepository balanceRepository;

  @HandleBeforeCreate
  private void handleBefore(Account account) {
    if (StringUtils.isBlank(account.getUsername())) {
      account.setUsername(SecurityContextHolder.getContext().getAuthentication().getName());
    }
  }

  @HandleBeforeSave
  @HandleAfterCreate
  private void handleAfterCreateAccount(Account account) {
    account.getBalances().forEach(bal -> {
      bal.setAccount(account);
      this.balanceRepository.save(bal);
    });
  }
}
