package com.a18.auth.model.repository;

import com.a18.auth.model.Authority;
import com.a18.common.constant.Privilege;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.prepost.PreAuthorize;

@PreAuthorize("hasAuthority('" + Privilege.READ + Privilege.AUTHORITY + "')")
public interface AuthorityRepository extends JpaRepository<Authority, Integer> {
  @PreAuthorize("hasAuthority('" + Privilege.WRITE + Privilege.AUTHORITY + "')")
  @Override <S extends Authority> S save(S entity);

  @PreAuthorize("hasAuthority('" + Privilege.EXEC + Privilege.AUTHORITY + "')")
  @Override void delete(Authority entity);
}
