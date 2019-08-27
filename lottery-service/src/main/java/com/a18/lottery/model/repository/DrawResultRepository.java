package com.a18.lottery.model.repository;

import com.a18.common.constant.Privilege;
import com.a18.lottery.model.DrawResult;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.security.access.prepost.PreAuthorize;

public interface DrawResultRepository
    extends JpaRepository<DrawResult, Long>, JpaSpecificationExecutor<DrawResult> {
  @PreAuthorize("hasAuthority('" + Privilege.WRITE + Privilege.DRAW_RESULT + "')")
  @Override <S extends DrawResult> S save(@NotNull S entity);

  @PreAuthorize("hasAuthority('" + Privilege.EXEC + Privilege.DRAW_RESULT + "') ")
  @Override void delete(@NotNull DrawResult entity);

  Set<DrawResult> getAllByIssueId(Long issueId);
}
