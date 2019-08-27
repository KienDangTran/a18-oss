package com.a18.account.model.repository;

import com.a18.account.model.Turnover;
import com.a18.common.constant.Ccy;
import com.a18.common.constant.GameCategory;
import com.a18.common.constant.Journal;
import com.a18.common.constant.Privilege;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;

public interface TurnoverRepository extends JpaRepository<Turnover, Integer> {
  @PreAuthorize("hasAuthority('" + Privilege.WRITE + Privilege.TURNOVER + "') ")
  @Override <S extends Turnover> S save(@NotNull S entity);

  @PreAuthorize("hasAuthority('" + Privilege.EXEC + Privilege.TURNOVER + "') ")
  @Override void delete(@NotNull Turnover entity);

  Optional<Turnover> findByJournalAndGameCategoryAndCcy(
      @NotNull @Param("journal") Journal journal,
      @NotNull @Param("gameCategory") GameCategory gameCategory,
      @NotNull @Param("ccy") Ccy ccy
  );
}
