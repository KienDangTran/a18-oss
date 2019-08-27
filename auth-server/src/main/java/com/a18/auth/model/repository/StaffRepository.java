package com.a18.auth.model.repository;

import com.a18.auth.model.Staff;
import com.a18.common.constant.Privilege;
import java.util.List;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;

public interface StaffRepository extends UserDetailsRepository<Staff, Long> {
  @PreAuthorize("hasAuthority('" + Privilege.READ + Privilege.STAFF + "')")
  @Override Page<Staff> findAll(Pageable pageable);

  @PreAuthorize("hasAuthority('" + Privilege.READ + Privilege.STAFF + "')")
  @Override List<Staff> findAll(Sort sort);

  @PostAuthorize("hasAuthority('" + Privilege.READ + Privilege.STAFF + "') "
                     + "or (returnObject.isPresent() and returnObject.get().username.equals(authentication.name))")
  @Override Optional<Staff> findById(Long id);

  @PreAuthorize("hasAuthority('" + Privilege.WRITE + Privilege.STAFF + "') "
                    + "or #entity.username.equals(authentication.name)")
  @Override <S extends Staff> S save(@NotNull @P("entity") S entity);

  @PreAuthorize("hasAuthority('" + Privilege.EXEC + Privilege.STAFF + "')")
  @Override void delete(Staff staff);
}
