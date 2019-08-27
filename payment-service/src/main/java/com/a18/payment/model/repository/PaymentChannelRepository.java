package com.a18.payment.model.repository;

import com.a18.common.constant.Privilege;
import com.a18.payment.model.PaymentChannel;
import javax.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;

@RepositoryRestResource
public interface PaymentChannelRepository
    extends JpaRepository<PaymentChannel, Integer>, JpaSpecificationExecutor<PaymentChannel> {
  @PreAuthorize("hasAuthority('" + Privilege.WRITE + Privilege.PAYMENT_CHANNEL + "') ")
  @Override <S extends PaymentChannel> S save(@NotNull @P("entity") S entity);

  @PreAuthorize("hasAuthority('" + Privilege.EXEC + Privilege.PAYMENT_CHANNEL + "')")
  @Override void delete(@NotNull PaymentChannel paymentChannel);
}

