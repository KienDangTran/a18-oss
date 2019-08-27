package com.a18.lottery.model.repository;

import com.a18.common.constant.Privilege;
import com.a18.lottery.model.Rule;
import java.util.Set;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PreAuthorize;

public interface RuleRepository extends JpaRepository<Rule, Integer> {
  @PreAuthorize("hasAuthority('" + Privilege.WRITE + Privilege.RULE + "')")
  @Override <S extends Rule> S save(@NotNull S entity);

  @PreAuthorize("hasAuthority('" + Privilege.EXEC + Privilege.RULE + "') ")
  @Override void delete(@NotNull Rule entity);

  @RestResource(exported = false)
  Set<Rule> findAllByLotterySchemaIdAndStatusIn(
      @NotNull Integer lotterySchemaId,
      @NotEmpty Set<Rule.RuleStatus> statuses
  );

  @RestResource(exported = false)
  Set<Rule> findAllByPrizeSchemasIdAndLotterySchemaIdInAndStatusIn(
      @NotNull Integer prizeSchemasId,
      @NotNull Set<Integer> lotterySchemaIds,
      @NotEmpty Set<Rule.RuleStatus> statuses
  );
}
