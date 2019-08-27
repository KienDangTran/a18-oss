package com.a18.lottery.model.repository;

import com.a18.common.constant.Privilege;
import com.a18.lottery.model.LotteryCategory;
import com.a18.lottery.model.Scheduler;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;

public interface SchedulerRepository
    extends JpaRepository<Scheduler, Integer>, JpaSpecificationExecutor<Scheduler> {

  @PreAuthorize("hasAuthority('" + Privilege.WRITE + Privilege.SCHEDULER + "') ")
  @Override <S extends Scheduler> S save(@NotNull S entity);

  @PreAuthorize("hasAuthority('" + Privilege.EXEC + Privilege.SCHEDULER + "') ")
  @Override void delete(@NotNull Scheduler entity);

  Set<Scheduler> findAllByCategoryInAndStatusIn(
      @NotNull @Param("categories") Set<LotteryCategory> categories,
      @NotNull @Param("statuses") Set<Scheduler.SchedulerStatus> statuses
  );
}
