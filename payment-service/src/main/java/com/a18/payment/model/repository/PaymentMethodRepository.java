package com.a18.payment.model.repository;

import com.a18.common.constant.Privilege;
import com.a18.payment.model.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.security.access.prepost.PreAuthorize;

public interface PaymentMethodRepository
    extends JpaRepository<PaymentMethod, Integer>, JpaSpecificationExecutor<PaymentMethod> {
  @PreAuthorize("hasAuthority('" + Privilege.WRITE + Privilege.PAYMENT_METHOD + "') ")
  @Override <S extends PaymentMethod> S save(S entity);

  @PreAuthorize("hasAuthority('" + Privilege.EXEC + Privilege.PAYMENT_METHOD + "') ")
  @Override void delete(PaymentMethod entity);
}
