package com.a18.payment.validator;

import com.a18.payment.model.Bank;
import com.a18.payment.model.PaymentChannel;
import com.a18.payment.model.PaymentMethod;
import com.a18.payment.model.PaymentVendor;
import com.a18.payment.model.dto.TxDTO;
import com.a18.payment.model.repository.PaymentChannelRepository;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
@Lazy
public class BeforeCreateTxValidator implements Validator {

  @Autowired @Lazy private DepositValidator depositValidator;

  @Autowired @Lazy private WithdrawalValidator withdrawalValidator;

  @Autowired @Lazy private PaymentChannelRepository channelRepository;

  @Override public boolean supports(Class<?> clazz) {
    return TxDTO.class.isAssignableFrom(clazz);
  }

  @Transactional(readOnly = true)
  @Override public void validate(Object target, Errors errors) {
    TxDTO txDTO = (TxDTO) target;

    if (!this.commonValidate(txDTO, errors)) return;

    switch (txDTO.getJournal()) {
      case DEPOSIT:
        this.depositValidator.validate(txDTO, errors);
        break;
      case WITHDRAWAL:
        this.withdrawalValidator.validate(txDTO, errors);
        break;
      default:
        throw new UnsupportedOperationException(
            "Journal not supported: " + txDTO.getJournal().name()
        );
    }
  }

  private boolean commonValidate(TxDTO txDTO, Errors errors) {
    this.validateRequireFields(errors);

    if (!errors.hasErrors()) {
      this.validatePaymentChannel(txDTO, errors)
          .ifPresent(channel -> this.validateAmt(channel, txDTO.getAmt(), errors));
    }

    return !errors.hasErrors();
  }

  private void validateRequireFields(Errors errors) {
    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "paymentChannelId",
        "common.field.required",
        new Object[] {"paymentChannelId"}
    );
    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "gameCategory",
        "common.field.required",
        new Object[] {"gameCategory"}
    );
    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "amt",
        "common.field.required",
        new Object[] {"amt"}
    );
    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "journal",
        "common.field.required",
        new Object[] {"journal"}
    );
  }

  private Optional<PaymentChannel> validatePaymentChannel(TxDTO txDTO, Errors errors) {
    Optional<PaymentChannel> channel = this.channelRepository
        .findById(txDTO.getPaymentChannelId());
    if (!channel.isPresent()) {
      errors.rejectValue("paymentChannelId", "payment.channel.not.found");
      return Optional.empty();
    }

    if (channel.get().getBank() == null ||
        !Objects.equals(Bank.BankStatus.ACTIVE, channel.get().getBank().getStatus())) {
      errors.rejectValue("paymentChannelId", "payment.bank.not.found.or.inactive");
    }

    if (channel.get().getPaymentMethod() == null
        || !Objects.equals(
        PaymentMethod.PaymentMethodStatus.ACTIVE,
        channel.get().getPaymentMethod().getStatus()
    )) {
      errors.rejectValue("paymentChannelId", "payment.method.not.found.or.inactive");
    }

    if (channel.get().getPaymentVendor() == null
        || !Objects.equals(
        PaymentVendor.PaymentVendorStatus.ACTIVE,
        channel.get().getPaymentVendor().getStatus()
    )) {
      errors.rejectValue("paymentChannelId", "payment.vendor.not.found.or.inactive");
    }

    return channel;
  }

  private void validateAmt(PaymentChannel channel, BigDecimal amt, Errors errors) {
    if (channel.getMinAmt() != null && amt.compareTo(channel.getMinAmt()) < 0) {
      errors.rejectValue(
          "amt",
          "tx.amt.must.be.greater.than",
          new Object[] {channel.getMinAmt().toString()},
          "tx.amt.must.be.greater.than"
      );
    }

    if (channel.getMaxAmt() != null && amt.compareTo(channel.getMaxAmt()) > 0) {
      errors.rejectValue(
          "amt",
          "tx.amt.must.be.less.than",
          new Object[] {channel.getMaxAmt().toString()},
          "tx.amt.must.be.less.than"
      );
    }
  }
}
