package com.a18.account.model.repository;

import com.a18.account.model.Balance;
import com.a18.account.model.JournalEntry;
import com.a18.common.constant.Journal;
import com.a18.common.constant.Privilege;
import java.util.Optional;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PostAuthorize;

public interface JournalEntryRepository
    extends JpaRepository<JournalEntry, Long>, JpaSpecificationExecutor<JournalEntry> {

  @PostAuthorize("hasAuthority('" + Privilege.READ + Privilege.JOURNAL_ENTRY + "') "
                     + "or returnObject.get().balance.account.username.equals(authentication.name)")
  @Override Optional<JournalEntry> findById(@NotNull Long id);

  @RestResource(exported = false)
  @Override <S extends JournalEntry> S save(@NotNull S entity);

  @RestResource(exported = false)
  @Override void delete(@NotNull JournalEntry entity);

  boolean existsByBalanceAndJournalAndRefIdAndRefType(
      @NotNull Balance balance,
      @NotNull Journal journal,
      @NotNull Long refId,
      @NotEmpty String refType
  );
}
