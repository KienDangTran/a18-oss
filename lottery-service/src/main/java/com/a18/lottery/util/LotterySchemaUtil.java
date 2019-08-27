package com.a18.lottery.util;

import com.a18.lottery.model.Lottery;
import java.math.BigDecimal;
import java.util.Objects;
import lombok.experimental.UtilityClass;
import org.springframework.util.Assert;

@UtilityClass
public class LotterySchemaUtil {
  public static BigDecimal getLotteryBetUnitPrice(Lottery lottery) {
    Assert.notNull(lottery, "cannot getLotteryBetUnitPrice 'coz lottery is null");
    return lottery.getBetUnitPrice() != null
           ? lottery.getBetUnitPrice()
           : Objects.requireNonNullElse(
               lottery.getLotterySchema().getDefaultBetUnitPrice(),
               BigDecimal.ZERO
           );
  }

  public static BigDecimal getLotteryWinUnitPrice(Lottery lottery) {
    Assert.notNull(lottery, "cannot getLotteryWinUnitPrice 'coz lottery is null");
    return lottery.getWinUnitPrice() != null
           ? lottery.getWinUnitPrice()
           : Objects.requireNonNullElse(
               lottery.getLotterySchema().getDefaultWinUnitPrice(),
               BigDecimal.ZERO
           );
  }

  public static BigDecimal getLotteryBetItemMaxAmt(Lottery lottery) {
    Assert.notNull(lottery, "cannot getLotteryBetItemMaxAmt 'coz lottery is null");
    return lottery.getBetItemMaxAmt() != null
           ? lottery.getBetItemMaxAmt()
           : Objects.requireNonNullElse(
               lottery.getLotterySchema().getDefaultBetItemMaxAmt(),
               BigDecimal.valueOf(Double.MAX_VALUE)
           );
  }

  public static Integer getLotteryMaxBetItem(Lottery lottery) {
    Assert.notNull(lottery, "cannot getLotteryMaxBetItem 'coz lottery is null");
    return lottery.getMaxBetItem() != null
           ? lottery.getMaxBetItem()
           : Objects.requireNonNullElse(lottery.getLotterySchema().getDefaultMaxBetItem(), 0);
  }

  public static BigDecimal getLotteryMaxPayout(Lottery lottery) {
    Assert.notNull(lottery, "cannot getLotteryMaxPayout 'coz lottery is null");
    return lottery.getMaxPayout() != null
           ? lottery.getMaxPayout()
           : Objects.requireNonNullElse(
               lottery.getLotterySchema().getDefaultMaxPayout(),
               BigDecimal.valueOf(Double.MAX_VALUE)
           );
  }

  public static Integer getLotteryBetItemSize(Lottery lottery) {
    return Objects.requireNonNullElse(lottery.getLotterySchema().getBetItemSize(), 0);
  }

  public static Integer getLotteryBetNoLength(Lottery lottery) {
    return Objects.requireNonNullElse(lottery.getLotterySchema().getBetNoLength(), 0);
  }
}
