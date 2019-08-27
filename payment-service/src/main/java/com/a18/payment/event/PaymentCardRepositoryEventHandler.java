package com.a18.payment.event;

import com.a18.payment.model.PaymentCard;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Lazy
@RepositoryEventHandler
public class PaymentCardRepositoryEventHandler {

  @Autowired private PasswordEncoder passwordEncoder;

  @HandleBeforeCreate
  public void handleBeforeCreatePaymentCard(PaymentCard card) {
    if (StringUtils.isNotBlank(card.getCvv())) {
      card.setCvv(StringUtils.trimToNull(this.passwordEncoder.encode(card.getCvv())));
    }
    card.setUsername(SecurityContextHolder.getContext().getAuthentication().getName());
    card.setOwnerName(StringUtils.trimToEmpty(card.getOwnerName()).toUpperCase());
  }
}
