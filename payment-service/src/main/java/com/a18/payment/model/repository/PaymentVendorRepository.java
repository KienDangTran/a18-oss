package com.a18.payment.model.repository;

import com.a18.common.constant.Privilege;
import com.a18.payment.model.PaymentVendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.prepost.PreAuthorize;

public interface PaymentVendorRepository extends JpaRepository<PaymentVendor, Integer> {
  @PreAuthorize("hasAuthority('" + Privilege.WRITE + Privilege.PAYMENT_VENDOR + "') ")
  @Override <S extends PaymentVendor> S save(S entity);

  @PreAuthorize("hasAuthority('" + Privilege.EXEC + Privilege.PAYMENT_VENDOR + "') ")
  @Override void delete(PaymentVendor entity);
}
