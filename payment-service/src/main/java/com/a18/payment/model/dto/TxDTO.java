package com.a18.payment.model.dto;

import com.a18.common.constant.GameCategory;
import com.a18.common.constant.Journal;
import com.a18.payment.model.PaymentChannel;
import com.a18.payment.model.Tx;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Wither;

@Getter
@AllArgsConstructor
@EqualsAndHashCode(of = {"code"})
@Wither
public final class TxDTO {

  public TxDTO(Tx tx) {
    this.id = tx.getId();
    this.code = tx.getCode();
    this.paymentChannelId = tx.getPaymentChannelId();
    this.paymentChannel = tx.getPaymentChannel();
    this.journal = tx.getJournal();
    this.username = tx.getUsername();
    this.amt = tx.getAmt();
    this.gameCategory = tx.getGameCategory();
    this.token = tx.getToken();
    this.invoiceNo = tx.getInvoiceNo();
    this.remark = tx.getRemark();
    this.errorCode = tx.getErrorCode();
    this.status = tx.getStatus();
    this.checkoutUrl = tx.getCheckoutUrl();
  }

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private final Long id;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private final String code;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private final String username;

  private final Integer paymentChannelId;

  @JsonIgnore
  private final PaymentChannel paymentChannel;

  private final Journal journal;

  private final GameCategory gameCategory;

  @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT)
  private final BigDecimal amt;

  private final String remark;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private final String token;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private final String checkoutUrl;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private final String errorCode;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private final String invoiceNo;

  private final Tx.TxStatus status;
}

