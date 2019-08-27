package com.a18.payment.validator;

import com.a18.common.constant.Ccy;
import com.a18.common.dto.BalanceDTO;
import com.a18.common.exception.ApiException;
import com.a18.common.util.AccountUtil;
import com.a18.payment.model.PaymentCard;
import com.a18.payment.model.PaymentChannel;
import com.a18.payment.model.dto.TxDTO;
import com.a18.payment.model.repository.PaymentCardRepository;
import com.a18.payment.model.repository.PaymentChannelRepository;
import com.a18.payment.model.repository.TxRepository;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
@Lazy
public class WithdrawalValidator {

  @Autowired @Lazy private PaymentChannelRepository providerRepository;

  @Autowired @Lazy private PaymentCardRepository cardRepository;

  @Autowired @Lazy private AccountUtil accountUtil;

  @Autowired @Lazy private TxRepository txRepository;

  void validate(@NotNull TxDTO txDTO, @NotNull Errors errors) {
    PaymentChannel channel = this.providerRepository
        .findById(txDTO.getPaymentChannelId())
        .orElseThrow(() -> new ApiException("payment.channel.not.found"));

    if (!channel.getWithdrawal()) {
      errors.rejectValue(
          "journal",
          "payment.channel.withdraw.not.supported",
          new Object[] {channel.getBank().getName(), channel.getPaymentMethod().getName()},
          "payment.channel.withdraw.not.supported"
      );
    }
    this.verifyAccount(txDTO, channel.getCcy(), errors);
    this.validateRequiredCardAndCcy(channel, channel.getCcy(), errors);
    this.validateRequiredBankAccountAndCcy(channel, channel.getCcy(), errors);
  }

  private void verifyAccount(@NotNull TxDTO txDTO, @NotNull Ccy ccy, @NotNull Errors errors) {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    this.accountUtil.findBalanceByUsernameAndCcyAndGameCategory(
        username,
        ccy,
        txDTO.getGameCategory()
    ).ifPresentOrElse(
        bal -> this.validateBalanceAmt(username, bal, txDTO.getAmt(), errors),
        () -> errors.reject(
            "account.username.not.found",
            new Object[] {username},
            "account.username.not.found"
        )
    );
  }

  private void validateBalanceAmt(
      String username,
      BalanceDTO bal,
      BigDecimal txAmt,
      Errors errors
  ) {
    BigDecimal pendingAmt = Objects.requireNonNullElse(
        this.txRepository.sumTotalPendingTxAmt(username),
        BigDecimal.ZERO
    );
    BigDecimal availAmt = bal.getBalance().subtract(bal.getOnHoldAmt()).subtract(pendingAmt);

    if (availAmt.compareTo(txAmt) < 0) {
      errors.rejectValue(
          "amt",
          "tx.withdrawal.amt.is.exceed.balance",
          new Object[] {bal.getBalance(), bal.getOnHoldAmt()},
          "tx.withdrawal.amt.is.exceed.balance"
      );
    }

    if (bal.getTurnoverAmt().compareTo(bal.getTotalBettingAmt()) > 0) {
      errors.rejectValue("amt", "tx.not.eligible.to.withdraw");
    }
  }

  private void validateRequiredCardAndCcy(
      @NotNull PaymentChannel provider,
      @NotNull Ccy ccy,
      @NotNull Errors errors
  ) {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();

    if (provider.getRequiredCardType() != null) {
      Optional<PaymentCard> activeCard =
          this.cardRepository.findByUsernameAndBankIdAndTypeAndCcyAndStatusIn(
              username,
              provider.getBank().getId(),
              provider.getRequiredCardType(),
              ccy,
              Set.of(PaymentCard.PaymentCardStatus.ACTIVE)
          );

      if (!activeCard.isPresent()) {
        errors.rejectValue(
            "paymentChannelId",
            "payment.method.require.card",
            new Object[] {
                provider.getPaymentMethod().getName(),
                provider.getBank().getName(),
                provider.getRequiredCardType().name(),
                ccy
            },
            "payment.method.require.card"
        );
      }
    }
  }

  private void validateRequiredBankAccountAndCcy(
      @NotNull PaymentChannel provider,
      @NotNull Ccy ccy,
      @NotNull Errors errors
  ) {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();

    if (provider.getBankAccountRequired()) {
      Set<PaymentCard> bankAcc =
          this.cardRepository.findAllByUsernameAndBankIdAndBankAccountNotNullAndCcyAndStatusIn(
              username,
              provider.getBank().getId(),
              ccy,
              Set.of(PaymentCard.PaymentCardStatus.ACTIVE)
          );

      if (bankAcc.isEmpty()) {
        errors.rejectValue(
            "paymentChannelId",
            "payment.method.require.bank.account",
            new Object[] {provider.getPaymentMethod().getName(), provider.getBank().getName(), ccy},
            "payment.method.require.bank.account"
        );
      } else {
        bankAcc.stream().findAny().ifPresent(anyAcc -> {
          if (bankAcc.stream().anyMatch(
              acc -> !anyAcc.getBankAccount().equalsIgnoreCase(acc.getBankAccount()))
          ) {
                errors.reject(
                    "payment.user.more.than.1.bank.account.no",
                    new Object[] {provider.getBank().getName(), ccy},
                    "payment.user.more.than.1.bank.account.no"
                );
              }
            }
        );
      }
    }
  }
}
