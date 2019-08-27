package com.a18.account.validator;

import com.a18.account.model.Account;
import com.a18.account.model.AccountCategory;
import com.a18.account.model.repository.BalanceRepository;
import com.a18.common.constant.Journal;
import com.a18.common.dto.JournalDTO;
import com.a18.common.exception.ApiException;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
@Lazy
public class JournalValidator implements Validator {

  @Autowired @Lazy private BalanceRepository balanceRepository;

  @Override public boolean supports(Class<?> clazz) {
    return JournalDTO.class.isAssignableFrom(clazz);
  }

  @Override public void validate(Object target, Errors errors) {
    JournalDTO msg = (JournalDTO) target;
    this.commonValidate(msg, errors);
    if (!errors.hasErrors()) {
      switch (msg.getJournal()) {
        case INVESTMENT:
          break;
        case DECREASING_ADJUSTMENT:
          this.assertUserBalanceGreaterThanJournalAmt(msg, errors);
          break;
        case INCREASING_ADJUSTMENT:
        case BONUS:
          this.assertCompanyAssetBalanceGreaterThanJournalAmt(msg, errors);
          break;
        case CANCEL_BONUS:
          this.assertUserBonusBalanceGreaterThanJournalAmt(msg, errors);
          break;
        default:
          errors.rejectValue(
              "journal",
              "journal.unsupported.manual.journal",
              new Object[] {msg.getJournal().name()},
              "journal.unsupported.manual.journal"
          );
      }
    }
  }

  private void commonValidate(JournalDTO msg, Errors errors) {
    this.validateRequireFields(msg, errors);
  }

  private void validateRequireFields(JournalDTO msg, Errors errors) {
    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "journal",
        "common.field.required",
        new Object[] {"journal"}
    );
    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "ccy",
        "common.field.required",
        new Object[] {"ccy"}
    );
    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "amt",
        "common.field.required",
        new Object[] {"amt"}
    );

    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "username",
        "common.field.required",
        new Object[] {"username"}
    );

    if (!Journal.INVESTMENT.equals(msg.getJournal())) {
      ValidationUtils.rejectIfEmptyOrWhitespace(
          errors,
          "gameCategory",
          "common.field.required",
          new Object[] {"gameCategory"}
      );
    }
  }

  private void assertCompanyAssetBalanceGreaterThanJournalAmt(JournalDTO msg, Errors errors) {
    this.balanceRepository
        .findByAccountCategoryInAndAccount_StatusIn(
            Set.of(AccountCategory.COMPANY_ASSET),
            Set.of(Account.AccountStatus.ACTIVE)
        )
        .ifPresentOrElse(
            balance -> {
              if (msg.getAmt().compareTo(balance.getBalance()) > 0) {
                errors.rejectValue(
                    "amt",
                    "journal.increasing.amt.greater.than.company.asset.balance"
                );
              }
            },
            () -> errors.reject("account.company.asset.account.not.found")
        );
  }

  private void assertUserBalanceGreaterThanJournalAmt(JournalDTO msg, Errors errors) {
    this.balanceRepository
        .findByAccount_UsernameAndCcyAndGameCategoryAndAccount_CategoryInAndAccount_StatusIn(
            msg.getUsername(),
            msg.getCcy(),
            msg.getGameCategory(),
            Set.of(AccountCategory.USER_ASSET),
            Set.of(Account.AccountStatus.ACTIVE)
        )
        .ifPresentOrElse(
            balance -> {
              if (msg.getAmt().compareTo(balance.getBalance()) > 0) {
                errors.rejectValue(
                    "amt",
                    "journal.decreasing.amt.greater.than.user.betting.balance"
                );
              }
            },
            () -> new ApiException("account.betting.balance.not.found")
        );
  }

  private void assertUserBonusBalanceGreaterThanJournalAmt(JournalDTO msg, Errors errors) {
    this.balanceRepository
        .findByAccount_UsernameAndCcyAndGameCategoryAndAccount_CategoryInAndAccount_StatusIn(
            msg.getUsername(),
            msg.getCcy(),
            msg.getGameCategory(),
            Set.of(AccountCategory.USER_ASSET),
            Set.of(Account.AccountStatus.ACTIVE)
        )
        .ifPresentOrElse(
            balance -> {
              if (msg.getAmt().compareTo(balance.getBonusBalance()) > 0) {
                errors.rejectValue(
                    "amt",
                    "journal.decreasing.amt.greater.than.user.bonus.balance"
                );
              }
            },
            () -> errors.reject("account.bonus.balance.not.found")
        );
  }
}
