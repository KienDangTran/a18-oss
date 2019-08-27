package com.a18.common.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.Wither;

@Value
@Builder
@Wither
@EqualsAndHashCode(of = {"code"})
public class PrizeDTO {
  Integer id;

  String code;

  Integer prizeSchemaId;

  Integer schedulerId;

  Integer prizePosition;

  Integer winNoSize;

  Integer winNoLength;
}
