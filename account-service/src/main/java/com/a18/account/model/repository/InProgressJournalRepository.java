package com.a18.account.model.repository;

import com.a18.account.model.InProgressJournal;
import com.a18.common.constant.Journal;
import com.a18.common.constant.Privilege;
import java.math.BigDecimal;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PostAuthorize;

public interface InProgressJournalRepository
    extends JpaRepository<InProgressJournal, Long>, JpaSpecificationExecutor<InProgressJournal> {

  @PostAuthorize("hasAuthority('" + Privilege.READ + Privilege.JOURNAL_ENTRY + "') ")
  @Override Optional<InProgressJournal> findById(@NotNull Long id);

  @RestResource(exported = false)
  @PostAuthorize("hasAuthority('" + Privilege.WRITE + Privilege.JOURNAL_ENTRY + "') ")
  @Override <S extends InProgressJournal> S save(@NotNull S entity);

  @PostAuthorize("hasAuthority('" + Privilege.EXEC + Privilege.JOURNAL_ENTRY + "') ")
  @Override void delete(@NotNull InProgressJournal entity);

  @RestResource(exported = false)
  Optional<InProgressJournal> getByBalanceIdAndJournalAndRefIdAndRefType(
      Long balanceId,
      Journal journal,
      Long refId,
      String refType
  );

  void deleteAllByBalanceIdAndJournalAndRefIdAndRefType(
      Long balanceId,
      Journal journal,
      Long refId,
      String refType
  );

  @RestResource(exported = false)
  @Query("SELECT SUM(j.amt) FROM InProgressJournal j WHERE j.balanceId = :balanceId")
  BigDecimal getTotalOnHoldAmt(Long balanceId);
}
