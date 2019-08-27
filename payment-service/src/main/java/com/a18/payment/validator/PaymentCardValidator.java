package com.a18.payment.validator;

import com.a18.common.dto.UserDTO;
import com.a18.common.exception.ApiException;
import com.a18.common.util.AuthUtil;
import com.a18.payment.model.PaymentCard;
import com.a18.payment.model.repository.PaymentCardRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class PaymentCardValidator implements Validator {
  @Autowired private AuthUtil authUtil;

  @Autowired private PaymentCardRepository cardRepository;

  @Override public boolean supports(Class<?> clazz) {
    return clazz.equals(PaymentCard.class);
  }

  @Override public void validate(Object target, Errors errors) {
    PaymentCard card = (PaymentCard) target;
    this.validateRequiredField(card, errors);
    if (!errors.hasErrors()) {
      this.validateOwnerName(card, errors);
      this.validateCardMonth(card.getCardMonth(), errors);
      this.validateCardYear(card.getCardYear(), errors);
      this.validateCardNo(card, errors);
    }
  }

  private void validateCardNo(PaymentCard card, Errors errors) {
    if (StringUtils.isBlank(card.getCardNo())) return;
    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "type",
        "common.field.required",
        new Object[] {"type"}
    );
  }

  private void validateRequiredField(PaymentCard card, Errors errors) {
    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "ccy",
        "common.field.required",
        new Object[] {"ccy"}
    );
    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "bankId",
        "common.field.required",
        new Object[] {"bankId"}
    );
    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "ownerName",
        "common.field.required",
        new Object[] {"ownerName"}
    );
    if (StringUtils.isBlank(card.getBankAccount()) && StringUtils.isBlank(card.getCardNo())) {
      ValidationUtils.rejectIfEmptyOrWhitespace(
          errors,
          "cardNo",
          "common.field.required",
          new Object[] {"cardNo/bankAccount"}
      );
    }
  }

  private void validateOwnerName(PaymentCard card, Errors errors) {
    String username = StringUtils.defaultIfBlank(
        card.getUsername(),
        SecurityContextHolder.getContext().getAuthentication().getName()
    );

    UserDTO userDTO =
        this.authUtil.retrieveUserInfoByUsername(username)
                     .orElseThrow(() -> new ApiException("user.info.cannot.retrieve"));

    if (!userDTO.getFullname().equalsIgnoreCase(card.getOwnerName())) {
      errors.rejectValue("ownerName", "paymentCard.ownerName.must.be.same.as.user.fullname");
    }

    if (this.cardRepository.findAllByUsername(userDTO.getUsername()).stream().anyMatch(
        persistedCard -> !StringUtils.trimToEmpty(persistedCard.getOwnerName())
                                     .toUpperCase()
                                     .equalsIgnoreCase(StringUtils.trimToEmpty(card.getOwnerName())
                                                                  .toUpperCase())
    )) {
      errors.rejectValue("ownerName", "paymentCard.ownerName.inconsistent");
    }
  }

  private void validateCardMonth(Integer cardMonth, Errors errors) {
    if (cardMonth == null || NumberUtils.INTEGER_ZERO >= cardMonth || 12 < cardMonth) {
      errors.rejectValue("cardMonth", "paymentCard.cardMonth.invalid");
    }
  }

  private void validateCardYear(Integer cardYear, Errors errors) {
    if (cardYear == null || cardYear <= NumberUtils.INTEGER_ZERO) {
      errors.rejectValue("cardYear", "paymentCard.cardYear.invalid");
    }
  }

  @Component("beforeCreatePaymentCardValidator")
  @Lazy
  public static class BeforeCreatePaymentCardValidator extends PaymentCardValidator {}

  @Component("beforeSavePaymentCardValidator")
  @Lazy
  public static class BeforeSavePaymentCardValidator extends PaymentCardValidator {}
}
