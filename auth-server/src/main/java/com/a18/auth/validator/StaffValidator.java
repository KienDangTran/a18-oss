package com.a18.auth.validator;

import com.a18.auth.model.Staff;
import com.a18.auth.model.repository.StaffRepository;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.stereotype.Component;

public class StaffValidator {
  @Component("beforeCreateStaffValidator")
  @Lazy
  public static class BeforeCreateStaffValidator extends UserDetailsCommonValidator<Staff, Long> {

    @Autowired
    public BeforeCreateStaffValidator(StaffRepository staffRepository, EntityManager em) {
      super(
          staffRepository,
          (JpaEntityInformation<Staff, Long>) JpaEntityInformationSupport.getEntityInformation(
              Staff.class,
              em
          )
      );
    }

    @Override public boolean supports(Class<?> clazz) {
      return Staff.class.equals(clazz);
    }
  }

  @Component("beforeSaveStaffValidator")
  @Lazy
  public static class BeforeSaveStaffValidator extends UserDetailsCommonValidator<Staff, Long> {

    @Autowired public BeforeSaveStaffValidator(StaffRepository staffRepository, EntityManager em) {
      super(
          staffRepository,
          (JpaEntityInformation<Staff, Long>) JpaEntityInformationSupport.getEntityInformation(
              Staff.class,
              em
          )
      );
    }

    @Override public boolean supports(Class<?> clazz) {
      return Staff.class.equals(clazz);
    }
  }
}

