package com.a18.lottery.model.repository;

import com.a18.common.constant.Privilege;
import com.a18.lottery.model.Lottery;
import javax.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.security.access.prepost.PreAuthorize;

public interface LotteryRepository
    extends JpaRepository<Lottery, Integer>, JpaSpecificationExecutor<Lottery> {
  @PreAuthorize("hasAuthority('" + Privilege.WRITE + Privilege.LOTTERY + "')")
  @Override <S extends Lottery> S save(@NotNull S entity);

  @PreAuthorize("hasAuthority('" + Privilege.EXEC + Privilege.LOTTERY + "') ")
  @Override void delete(@NotNull Lottery entity);
}
