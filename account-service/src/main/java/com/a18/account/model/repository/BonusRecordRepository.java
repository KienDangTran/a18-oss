package com.a18.account.model.repository;

import com.a18.account.model.Balance;
import com.a18.account.model.BonusRecord;
import com.a18.account.model.Promotion;
import com.a18.common.constant.Privilege;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PostAuthorize;

public interface BonusRecordRepository
    extends JpaRepository<BonusRecord, Long>, JpaSpecificationExecutor<BonusRecord> {
  @RestResource(exported = false)
  Optional<BonusRecord> findByBalanceAndPromotion(
      @NotNull Balance balance,
      @NotNull Promotion promotion
  );

  @PostAuthorize("hasAuthority('" + Privilege.READ + Privilege.ACCOUNT + "') "
                     + "or (returnObject.get() != null "
                     + "and returnObject.get().balance.account.username.equals(authentication.name))")
  @Override Optional<BonusRecord> findById(Long aLong);

  @RestResource(exported = false)
  @Override <S extends BonusRecord> S save(S entity);

  @RestResource(exported = false)
  @Override void delete(BonusRecord entity);
}
