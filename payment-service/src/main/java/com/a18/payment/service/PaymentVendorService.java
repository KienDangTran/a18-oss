package com.a18.payment.service;

import com.a18.payment.model.dto.TxDTO;

public interface PaymentVendorService {
  TxDTO sendDepositRequest();

  TxDTO sendWithdrawRequest();

  TxDTO getTransactionResult();
}
