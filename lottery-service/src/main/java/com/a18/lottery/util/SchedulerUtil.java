package com.a18.lottery.util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import lombok.experimental.UtilityClass;
import org.springframework.scheduling.support.CronTrigger;

@UtilityClass
public class SchedulerUtil {
  /**
   * Calculates next running time of a scheduler base on it's opening duration.
   */
  public static LocalDateTime computeQuickLotteryNextOpeningTime(
      LocalDateTime now,
      Integer openDuration
  ) {
    int secondsToNextRun =
        (now.get(ChronoField.SECOND_OF_DAY) / openDuration + 1) * openDuration
            - now.get(ChronoField.SECOND_OF_DAY);

    return now.plusSeconds(secondsToNextRun).truncatedTo(ChronoUnit.SECONDS);
  }

  public static CronTrigger buildCronTriggerAtSpecificTime(LocalDateTime time) {
    StringBuilder cronExpr = new StringBuilder();
    cronExpr.append(time.getSecond())
            .append(" ")
            .append(time.getMinute())
            .append(" ")
            .append(time.getHour())
            .append(" ")
            .append(time.getDayOfMonth())
            .append(" ")
            .append(time.getMonthValue())
            .append(" ")
            .append("?");

    return new CronTrigger(cronExpr.toString());
  }
}
