package com.a18.payment.validator;

import com.a18.common.constant.Ccy;
import com.a18.common.exception.ApiException;
import com.a18.common.util.AccountUtil;
import com.a18.payment.model.PaymentChannel;
import com.a18.payment.model.dto.TxDTO;
import com.a18.payment.model.repository.PaymentChannelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
@Lazy
public class DepositValidator {

  @Autowired @Lazy private PaymentChannelRepository channelRepository;

  @Autowired @Lazy private AccountUtil accountUtil;

  public void validate(TxDTO txDTO, Errors errors) {
    PaymentChannel channel = this.channelRepository
        .findById(txDTO.getPaymentChannelId())
        .orElseThrow(() -> new ApiException("payment.channel.not.found"));
    if (!channel.getDeposit()) {
      errors.rejectValue("journal", "payment.channel.deposit.not.supported");
    }
    this.verifyAccount(txDTO, channel.getCcy(), errors);
  }

  private void verifyAccount(TxDTO txDTO, Ccy ccy, Errors errors) {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    if (!this.accountUtil.findBalanceByUsernameAndCcyAndGameCategory(
        username,
        ccy,
        txDTO.getGameCategory()
    ).isPresent()) {
      errors.reject("account.does.not.have.betting.balance");
    }
  }
}
