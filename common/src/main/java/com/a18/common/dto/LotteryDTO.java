package com.a18.common.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.Wither;

@Value
@Builder
@Wither
@EqualsAndHashCode(of = {"code"})
public class LotteryDTO {

  Integer id;

  String code;

  Integer lotterySchemaId;

  Integer schedulerId;

  BigDecimal betUnitPrice;

  BigDecimal winUnitPrice;

  Integer maxBetItem;

  BigDecimal betItemMaxAmt;

  BigDecimal maxPayout;

  Integer betItemSize;

  Integer betNoLength;
}
