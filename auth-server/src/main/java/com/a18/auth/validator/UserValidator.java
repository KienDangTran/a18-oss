package com.a18.auth.validator;

import com.a18.auth.model.User;
import com.a18.auth.model.repository.UserRepository;
import java.time.LocalDate;
import javax.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Slf4j
@Component
@Lazy
public class UserValidator extends UserDetailsCommonValidator<User, Long> {

  @Autowired @Lazy
  public UserValidator(UserRepository userRepository, EntityManager em) {
    super(
        userRepository,
        (JpaEntityInformation<User, Long>) JpaEntityInformationSupport.getEntityInformation(
            User.class,
            em
        )
    );
  }

  @Override public void validate(Object target, Errors errors) {
    super.validate(target, errors);
    User user = (User) target;
    validateUserDocuments(user, errors);
  }

  private void validateUserDocuments(User user, Errors errors) {
    if (!user.getUserDocuments().isEmpty()) {
      user.getUserDocuments().forEach(doc -> {
        if (StringUtils.isBlank(doc.getCardNo()) || !doc.getCardNo().matches("^[A-Z0-9]+$")) {
          errors.rejectValue("userDocuments", "userDocuments.cardNo.invalid");
        }
        if (doc.getIssueDate() == null || !doc.getIssueDate().isBefore(LocalDate.now())) {
          errors.rejectValue("userDocuments", "userDocuments.issueDate.invalid");
        }
        if (doc.getExpireDate() != null && !doc.getExpireDate().isAfter(LocalDate.now())) {
          errors.rejectValue("userDocuments", "userDocuments.expireDate.invalid");
        }
        if (doc.getType() == null) {
          errors.rejectValue(
              "userDocuments",
              "common.field.required",
              new Object[] {"userDocuments.type"},
              "common.field.required"
          );
        }
      });
    }
  }

  @Override public boolean supports(Class<?> clazz) {
    return User.class.equals(clazz);
  }

  @Component("beforeCreateUserValidator")
  @Lazy
  public static class BeforeCreateUserValidator extends UserValidator {
    public BeforeCreateUserValidator(UserRepository userRepository, EntityManager em) {
      super(userRepository, em);
    }
  }

  @Component("beforeSaveUserValidator")
  @Lazy
  public static class BeforeSaveUserValidator extends UserValidator {
    public BeforeSaveUserValidator(UserRepository userRepository, EntityManager em) {
      super(userRepository, em);
    }
  }
}

