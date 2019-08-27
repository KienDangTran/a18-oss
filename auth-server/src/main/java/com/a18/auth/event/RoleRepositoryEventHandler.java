package com.a18.auth.event;

import com.a18.auth.model.Role;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler
@Lazy
public class RoleRepositoryEventHandler {
  @HandleBeforeCreate
  @HandleBeforeSave
  private void handleBefore(Role role) {
    role.getRoleAuthorities().forEach(roleAuthority -> roleAuthority.setRole(role));
  }
}
