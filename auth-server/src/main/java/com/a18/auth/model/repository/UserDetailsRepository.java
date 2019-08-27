package com.a18.auth.model.repository;

import com.a18.auth.model.AbstractUserDetails;
import com.a18.common.constant.Privilege;
import java.io.Serializable;
import java.util.Optional;
import javax.validation.constraints.NotEmpty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

@NoRepositoryBean
public interface UserDetailsRepository<T extends AbstractUserDetails, ID extends Serializable>
    extends JpaRepository<T, ID> {

  long countAllByUsername(String username);

  long countAllByEmail(String email);

  long countAllByPhone(String phone);

  @PreAuthorize("hasAuthority('" + Privilege.READ + Privilege.USER + "')"
                    + " or #username.equals(authentication.name)")
  Optional<T> findByUsername(@NotEmpty @Param("username") String username);

  @PostAuthorize("hasAuthority('" + Privilege.READ + Privilege.USER + "')"
                     + " or (returnObject.get() != null "
                     + " and returnObject.get().username.equals(authentication.name))")
  Optional<T> findByEmail(@NotEmpty @Param("email") String email);

  @PostAuthorize("hasAuthority('" + Privilege.READ + Privilege.USER + "')"
                     + " or (returnObject.get() != null "
                     + " and returnObject.get().username.equals(authentication.name))")
  Optional<T> findByPhone(@NotEmpty @Param("phone") String phone);

  @RestResource(exported = false)
  @Query("UPDATE #{#entityName} u SET u.password = ?1 WHERE u.id = ?2")
  @Modifying
  void changePassword(String encryptedPassword, ID id);

  @RestResource(exported = false)
  @Query("SELECT u.password FROM #{#entityName} u WHERE u.id = ?1")
  String getPassword(ID id);
}
