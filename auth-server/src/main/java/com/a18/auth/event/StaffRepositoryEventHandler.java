package com.a18.auth.event;

import com.a18.auth.model.Staff;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Lazy
@RepositoryEventHandler
public class StaffRepositoryEventHandler {
  @Autowired private PasswordEncoder passwordEncoder;

  @HandleBeforeCreate
  public void handleBeforeCreateStaff(Staff staff) {
    staff.setPassword(StringUtils.trimToNull(this.passwordEncoder.encode(staff.getPassword())));
    staff.getStaffRoles().forEach(staffRole -> staffRole.setStaff(staff));
  }

  @HandleBeforeSave
  public void handleBeforeSaveStaff(Staff staff) {
    staff.getStaffRoles().forEach(staffRole -> staffRole.setStaff(staff));
  }
}
