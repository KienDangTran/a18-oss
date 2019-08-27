package com.a18.account.model.repository;

import com.a18.account.model.Account.AccountStatus;
import com.a18.account.model.AccountCategory;
import com.a18.account.model.Balance;
import com.a18.common.constant.Ccy;
import com.a18.common.constant.GameCategory;
import java.util.Optional;
import java.util.Set;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface BalanceRepository extends CrudRepository<Balance, Long> {

  Optional<Balance> findByAccountCategoryInAndAccount_StatusIn(
      @NotNull Set<AccountCategory> categories,
      @NotEmpty Set<AccountStatus> statuses
  );

  Optional<Balance> findByAccount_UsernameAndCcyAndGameCategoryAndAccount_CategoryInAndAccount_StatusIn(
      @NotEmpty String username,
      @NotNull Ccy ccy,
      @NotNull GameCategory gameCategory,
      @NotEmpty Set<AccountCategory> categories,
      @NotEmpty Set<AccountStatus> statuses
  );
}
