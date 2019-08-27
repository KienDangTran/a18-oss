package com.a18.payment.model.repository;

import com.a18.common.constant.Ccy;
import com.a18.common.constant.Privilege;
import com.a18.payment.model.PaymentCard;
import com.a18.payment.model.PaymentCardType;
import java.util.Optional;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;

public interface PaymentCardRepository
    extends JpaRepository<PaymentCard, Long>, JpaSpecificationExecutor<PaymentCard> {

  @PostAuthorize("hasAuthority('" + Privilege.READ + Privilege.PAYMENT_CARD + "') "
                     + "or returnObject.get().username.equals(authentication.name)")
  @Override Optional<PaymentCard> findById(@NotNull Long id);

  @PreAuthorize("hasAuthority('" + Privilege.WRITE + Privilege.PAYMENT_CARD + "') "
                    + "or #entity.username.equals(authentication.name)")
  @Override <S extends PaymentCard> S save(@NotNull @P("entity") S entity);

  @PreAuthorize("hasAuthority('" + Privilege.EXEC + Privilege.PAYMENT_CARD + "') "
                    + " or #entity.username.equals(authentication.name)")
  @Override void delete(@NotNull @P("entity") PaymentCard entity);

  @PostAuthorize("hasAuthority('" + Privilege.READ + Privilege.PAYMENT_CARD + "') "
                     + " or #username.equals(authentication.name)")
  Set<PaymentCard> findAllByUsername(@NotNull @Param("username") String username);

  @PostAuthorize("hasAuthority('" + Privilege.READ + Privilege.PAYMENT_CARD + "') "
                     + " or #username.equals(authentication.name)")
  Set<PaymentCard> findAllByUsernameAndBankIdAndBankAccountNotNullAndCcyAndStatusIn(
      @NotNull @Param("username") String username,
      @NotNull @Param("bankId") Integer bankId,
      @NotNull @Param("ccy") Ccy ccy,
      @NotNull @Param("statuses") Set<PaymentCard.PaymentCardStatus> statuses
  );

  @PostAuthorize("hasAuthority('" + Privilege.READ + Privilege.PAYMENT_CARD + "') "
                     + " or #username.equals(authentication.name)")
  Optional<PaymentCard> findByUsernameAndBankIdAndTypeAndCcyAndStatusIn(
      @NotNull @Param("username") String username,
      @NotNull @Param("bankId") Integer bankId,
      @NotNull @Param("type") PaymentCardType type,
      @NotNull @Param("ccy") Ccy ccy,
      @NotNull @Param("statuses") Set<PaymentCard.PaymentCardStatus> statuses
  );
}
