package com.a18.account.model.repository;

import com.a18.account.model.Promotion;
import com.a18.common.constant.Ccy;
import com.a18.common.constant.GameCategory;
import com.a18.common.constant.Journal;
import com.a18.common.constant.Privilege;
import java.time.LocalDateTime;
import java.util.Set;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;

public interface PromotionRepository
    extends JpaRepository<Promotion, Integer>, JpaSpecificationExecutor<Promotion> {
  @PreAuthorize("hasAuthority('" + Privilege.WRITE + Privilege.PROMOTION + "') ")
  @Override <S extends Promotion> S save(@NotNull S entity);

  @PreAuthorize("hasAuthority('" + Privilege.EXEC + Privilege.PROMOTION + "') ")
  @Override void delete(@NotNull Promotion entity);

  @Query("select p "
             + " from Promotion p "
             + " where p.journal = :journal "
             + " and p.gameCategory = :gameCategory "
             + " and ccy = :ccy "
             + " and p.autoApply = :autoApply "
             + " and p.startTime <= :time"
             + " and (p.endTime is null or p.endTime > :time) "
             + " and p.status in :statuses")
  Set<Promotion> findAllApplyingPromotions(
      @NotNull @Param("journal") Journal journal,
      @NotNull @Param("gameCategory") GameCategory gameCategory,
      @NotNull @Param("ccy") Ccy ccy,
      @NotNull @Param("autoApply") Boolean autoApply,
      @NotNull @Param("time") LocalDateTime time,
      @NotEmpty @Param("statuses") Set<Promotion.PromotionStatus> statuses
  );
}
