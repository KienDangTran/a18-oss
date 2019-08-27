package com.a18.account.validator;

import com.a18.account.model.Account;
import com.a18.account.model.AccountCategory;
import com.a18.account.model.repository.AccountRepository;
import com.a18.common.dto.UserDTO;
import com.a18.common.exception.ApiException;
import com.a18.common.util.AuthUtil;
import javax.persistence.EntityManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public abstract class AccountValidator implements Validator {

  private final AccountRepository accountRepository;

  private final AuthUtil authUtil;

  private final JpaEntityInformation<Account, ?> entityInfo;

  public AccountValidator(
      AccountRepository accountRepository,
      AuthUtil authUtil,
      EntityManager em
  ) {
    this.accountRepository = accountRepository;
    this.authUtil = authUtil;
    this.entityInfo = JpaEntityInformationSupport.getEntityInformation(Account.class, em);
  }

  @Override public boolean supports(Class<?> clazz) {
    return Account.class.equals(clazz);
  }

  @Override public void validate(Object target, Errors errors) {
    Account account = (Account) target;
    this.validateUser(account, errors);
    this.validateBalances(account, errors);
  }

  private void validateUser(Account account, Errors errors) {
    String username = StringUtils.defaultIfBlank(
        account.getUsername(),
        SecurityContextHolder.getContext().getAuthentication().getName()
    );
    UserDTO userDTO = authUtil.retrieveUserInfoByUsername(username)
                              .orElseThrow(() -> new ApiException(
                                  "user.not.found"));
    if (this.entityInfo.isNew(account)
        && this.accountRepository.countAllByUsernameAndCategory(
        userDTO.getUsername(),
        account.getCategory()
    ) > 0) {
      errors.rejectValue(
          "username",
          "account.user.account.already.existed",
          new Object[] {userDTO.getUsername()},
          "account.user.account.already.existed"
      );
    }
  }

  private void validateBalances(Account account, Errors errors) {
    if (account.getBalances().isEmpty()) {
      errors.reject("account.must.contain.at.least.one.balance.currency");
      return;
    }
    account.getBalances()
           .stream()
           .filter(bal -> AccountCategory.USER_ASSET.equals(account.getCategory())
               && (bal.getCcy() == null || bal.getGameCategory() == null))
           .forEach(bal -> errors.reject("account.user.balance.ccy.gameCategory.cannot.be.null"));
  }

  @Component("beforeCreateAccountValidator")
  @Lazy
  public static class BeforeCreateAccountValidator extends AccountValidator {
    @Autowired @Lazy
    public BeforeCreateAccountValidator(
        AccountRepository accountRepository,
        AuthUtil authUtil,
        EntityManager em
    ) {
      super(accountRepository, authUtil, em);
    }
  }

  @Component("beforeSaveAccountValidator")
  @Lazy
  public static class BeforeSaveAccountValidator extends AccountValidator {
    @Autowired @Lazy
    public BeforeSaveAccountValidator(
        AccountRepository accountRepository,
        AuthUtil authUtil,
        EntityManager em
    ) {
      super(accountRepository, authUtil, em);
    }
  }
}
