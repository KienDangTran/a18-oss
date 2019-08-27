package com.a18.lottery.model.repository;

import com.a18.common.constant.Privilege;
import com.a18.lottery.model.Prize;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.security.access.prepost.PreAuthorize;

public interface PrizeRepository
    extends JpaRepository<Prize, Integer>, JpaSpecificationExecutor<Prize> {
  @PreAuthorize("hasAuthority('" + Privilege.WRITE + Privilege.PRIZE + "')")
  @Override <S extends Prize> S save(@NotNull S entity);

  @PreAuthorize("hasAuthority('" + Privilege.EXEC + Privilege.PRIZE + "') ")
  @Override void delete(@NotNull Prize entity);

  Set<Prize> getAllBySchedulerIdAndStatusIn(int schedulerId, Set<Prize.PrizeStatus> statuses);
}
