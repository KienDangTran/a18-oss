package com.a18.common.dto;

import com.a18.common.constant.Ccy;
import com.a18.common.constant.GameCategory;
import com.a18.common.constant.Journal;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.Wither;

@Value
@EqualsAndHashCode(of = {"journal", "username", "gameCategory", "ccy", "refId", "refType"})
@Builder
@Wither
public class JournalDTO {

  Journal journal;

  String username;

  GameCategory gameCategory;

  Ccy ccy;

  BigDecimal amt;

  BigDecimal additionalTurnoverAmt;

  Long refId;

  String refType;

  String registrationTokens;

  JournalStatus status;

  public enum JournalStatus {
    IN_PROGRESS, FINAL, CANCELED
  }
}
