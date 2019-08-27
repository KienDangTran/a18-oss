package com.a18.account.model.repository;

import com.a18.account.model.Account;
import com.a18.account.model.AccountCategory;
import com.a18.common.constant.Privilege;
import java.util.Optional;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;

public interface AccountRepository
    extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {

  @PostAuthorize("hasAuthority('" + Privilege.READ + Privilege.ACCOUNT + "') "
                     + "or returnObject.get().username.equals(authentication.name)")
  @Override Optional<Account> findById(@NotNull Long id);

  @PreAuthorize("hasAuthority('" + Privilege.WRITE + Privilege.ACCOUNT + "') "
                    + "or #entity.username.equals(authentication.name)")
  @Override <S extends Account> S save(@NotNull @P("entity") S entity);

  @PreAuthorize("hasAuthority('" + Privilege.EXEC + Privilege.ACCOUNT + "') "
                    + "or #entity.username.equals(authentication.name)")
  @Override void delete(@NotNull @P("entity") Account entity);

  @PostAuthorize("hasAuthority('" + Privilege.READ + Privilege.ACCOUNT + "') "
                     + " or #username.equals(authentication.name)")
  long countAllByUsernameAndCategory(
      @NotEmpty @Param("username") String username,
      @NotNull @Param("category") AccountCategory category
  );
}
