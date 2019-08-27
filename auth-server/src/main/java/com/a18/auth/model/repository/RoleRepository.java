package com.a18.auth.model.repository;

import com.a18.auth.model.Role;
import com.a18.common.constant.Privilege;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.prepost.PreAuthorize;

@PreAuthorize("hasAuthority('" + Privilege.READ + Privilege.ROLE + "')")
public interface RoleRepository extends JpaRepository<Role, Integer> {
  @PreAuthorize("hasAuthority('" + Privilege.WRITE + Privilege.ROLE + "')")
  @Override <S extends Role> S save(S entity);

  @PreAuthorize("hasAuthority('" + Privilege.EXEC + Privilege.ROLE + "')")
  @Override void delete(Role entity);
}
