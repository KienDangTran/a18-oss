package com.a18.common.dto;

import com.a18.common.constant.Ccy;
import com.a18.common.constant.GameCategory;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = {"accountId", "ccy", "gameCategory"})
@Builder
public class BalanceDTO {

  Long accountId;

  Ccy ccy;

  GameCategory gameCategory;

  Integer withdrawLimit;

  BigDecimal balance;

  BigDecimal onHoldAmt;

  BigDecimal bonusBalance;

  BigDecimal turnoverAmt;

  BigDecimal totalBettingAmt;

  public BalanceDTO(
      @JsonProperty("accountId") Long accountId,
      @JsonProperty("ccy") Ccy ccy,
      @JsonProperty("gameCategory") GameCategory gameCategory,
      @JsonProperty("withdrawLimit") Integer withdrawLimit,
      @JsonProperty("balance") BigDecimal balance,
      @JsonProperty("onHoldAmt") BigDecimal onHoldAmt,
      @JsonProperty("bonusBalance") BigDecimal bonusBalance,
      @JsonProperty("turnoverAmt") BigDecimal turnoverAmt,
      @JsonProperty("totalBettingAmt") BigDecimal totalBettingAmt
  ) {
    this.accountId = accountId;
    this.ccy = ccy;
    this.gameCategory = gameCategory;
    this.withdrawLimit = withdrawLimit;
    this.balance = balance;
    this.onHoldAmt = onHoldAmt;
    this.bonusBalance = bonusBalance;
    this.turnoverAmt = turnoverAmt;
    this.totalBettingAmt = totalBettingAmt;
  }
}
