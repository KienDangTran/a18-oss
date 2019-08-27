package com.a18.auth.model.projection;

import com.a18.auth.model.Role;
import com.a18.auth.model.Staff;
import java.time.LocalDate;
import java.util.Set;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "expand", types = Staff.class)
public interface StaffProjection {

  Long getId();

  String getUsername();

  String getFullname();

  String getEmail();

  String getPhone();

  LocalDate getDateOfBirth();

  Set<Role> getRoles();
}
