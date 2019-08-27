package com.a18.lottery.model.dto;

import com.a18.lottery.model.BetItemGroup;
import com.a18.lottery.model.WonItem;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class BetItemGroupDTO {
  BetItemGroupDTO(BetItemGroup betItemGroup) {
    this.betUnit = betItemGroup.getBetUnit();
    this.betItems = betItemGroup.getBetItems();
    this.wonItems = Set.of();
  }

  private final Integer betUnit;

  private final String betItems;

  private final Set<WonItem> wonItems;
}
