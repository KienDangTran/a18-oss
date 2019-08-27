package com.a18.payment.model.repository;

import com.a18.common.constant.Privilege;
import com.a18.payment.model.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.security.access.prepost.PreAuthorize;

public interface BankRepository
    extends JpaRepository<Bank, Integer>, JpaSpecificationExecutor<Bank> {
  @PreAuthorize("hasAuthority('" + Privilege.WRITE + Privilege.BANK + "') ")
  @Override <S extends Bank> S save(S entity);

  @PreAuthorize("hasAuthority('" + Privilege.EXEC + Privilege.BANK + "') ")
  @Override void delete(Bank entity);
}
