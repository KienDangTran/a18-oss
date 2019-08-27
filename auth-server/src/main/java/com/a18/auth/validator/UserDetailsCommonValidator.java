package com.a18.auth.validator;

import com.a18.auth.model.AbstractUserDetails;
import com.a18.auth.model.repository.UserDetailsRepository;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import java.io.Serializable;
import java.time.LocalDate;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public abstract class UserDetailsCommonValidator<T extends AbstractUserDetails, ID extends Serializable>
    implements Validator {
  /**
   * <pre>
   * ^                 # start-of-string
   * (?=.*[0-9])       # a digit must occur at least once
   * (?=.*[a-z])       # a lower case letter must occur at least once
   * (?=.*[A-Z])       # an upper case letter must occur at least once
   * (?=\S+$)          # no whitespace allowed in the entire string
   * .{8,}             # anything, at least eight places though
   * $                 # end-of-string
   * </pre>
   */
  public static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$";

  public static final String EMAIL_REGEX =
      "^[\\w-+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$";

  private UserDetailsRepository userDetailsRepository;

  protected JpaEntityInformation<T, ID> entityInfo;

  public UserDetailsCommonValidator(
      UserDetailsRepository userDetailRepository,
      JpaEntityInformation<T, ID> entityInfo
  ) {
    this.userDetailsRepository = userDetailRepository;
    this.entityInfo = entityInfo;
  }

  @Override public void validate(Object target, Errors errors) {
    Assert.isTrue(userDetailsRepository != null, "userDetailsRepository cannot be null");
    Assert.isTrue(entityInfo != null, "entityInfo cannot be null");

    T user = (T) target;
    this.validateRequiredFields(errors);
    if (!errors.hasErrors()) {
      this.validateUsername(user, errors);
      this.validatePassword(user.getPassword(), errors);
      this.validateFullname(user.getFullname(), errors);
      this.validateEmail(user, errors);
      this.validatePhone(user, errors);
      this.validateDateOfBirth(user.getDateOfBirth(), errors);
    }
  }

  private void validateRequiredFields(Errors errors) {
    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "username",
        "common.field.required",
        new Object[] {"username"}
    );
    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "password",
        "common.field.required",
        new Object[] {"password"}
    );
    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "fullname",
        "common.field.required",
        new Object[] {"fullname"}
    );
    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "email",
        "common.field.required",
        new Object[] {"email"}
    );
    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "phone",
        "common.field.required",
        new Object[] {"phone"}
    );
  }

  protected void validateUsername(T user, Errors errors) {
    if (this.entityInfo.isNew(user)
        && this.userDetailsRepository.countAllByUsername(user.getUsername()) > 0) {
      errors.rejectValue(
          "username",
          "user.info.username.existed",
          new Object[] {user.getUsername()},
          "user.info.username.existed"
      );
    }
    if (!user.getUsername().matches("^[a-zA-Z0-9_.-]{3,25}$")) {
      errors.rejectValue(
          "username",
          "user.info.username.invalid",
          new Object[] {user.getUsername()},
          "user.info.username.invalid"
      );
    }
  }

  private void validatePassword(String password, Errors errors) {
    if (!password.matches(PASSWORD_REGEX)) {
      errors.rejectValue(
          "password",
          "user.info.password.invalid"
      );
    }
  }

  protected void validateFullname(String fullname, Errors errors) {
    if (!fullname.matches("^[A-Za-z ]+$")) {
      errors.rejectValue(
          "fullname",
          "user.info.fullname.invalid"
      );
    }
  }

  protected void validateEmail(T user, Errors errors) {
    if (this.entityInfo.isNew(user)
        && this.userDetailsRepository.countAllByEmail(user.getEmail()) > 0) {
      errors.rejectValue(
          "email",
          "user.info.email.used",
          new Object[] {user.getEmail()},
          "user.info.email.used"
      );
    } else if (!user.getEmail().matches(EMAIL_REGEX)) {
      errors.rejectValue(
          "email",
          "user.info.email.invalid",
          new Object[] {user.getEmail()},
          "user.info.email.invalid"
      );
    }
  }

  protected void validatePhone(T user, Errors errors) {
    PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
    Phonenumber.PhoneNumber number;
    try {
      number = phoneNumberUtil.parse(user.getPhone(), LocaleContextHolder.getLocale().getCountry());
      String phone = phoneNumberUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.E164);
      if (this.entityInfo.isNew(user) && this.userDetailsRepository.countAllByPhone(phone) > 0) {
        errors.rejectValue(
            "phone",
            "user.info.phone.used",
            new Object[] {phone},
            "user.info.phone.used"
        );
      }
      user.setPhone(phone);
    } catch (NumberParseException e) {
      errors.rejectValue("phone", "user.info.phone.invalid");
    }
  }

  private void validateDateOfBirth(LocalDate dateOfBirth, Errors errors) {
    if (dateOfBirth != null && !dateOfBirth.isBefore(LocalDate.now())) {
      errors.rejectValue(
          "dateOfBirth",
          "user.dateOfBirth.invalid",
          new Object[] {dateOfBirth},
          "user.dateOfBirth.invalid"
      );
    }
  }
}
