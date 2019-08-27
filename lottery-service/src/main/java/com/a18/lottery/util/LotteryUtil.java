package com.a18.lottery.util;

import com.a18.common.dto.LotteryIssueDTO;
import com.a18.lottery.model.DrawResult;
import com.a18.lottery.model.Issue;
import com.a18.lottery.model.Prize;
import com.a18.lottery.model.PrizeSchema;
import com.a18.lottery.model.Scheduler;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

@UtilityClass
public class LotteryUtil {
  public static String buildLotteryIssueCode(
      Scheduler scheduler,
      LocalDate issueDate,
      Integer index
  ) {
    Assert.notNull(scheduler, "cannot buildLotteryIssueCode 'coz scheduler is null");
    Assert.notNull(issueDate, "cannot buildLotteryIssueCode 'coz issueDate is null");

    if (StringUtils.isBlank(scheduler.getCode())) return "";

    StringBuilder codeBuilder = new StringBuilder();
    String dateStr = issueDate.format(DateTimeFormatter.BASIC_ISO_DATE).substring(2, 8);
    String postfix = index != null ? StringUtils.leftPad(String.valueOf(index), 4, '0') : "";
    codeBuilder.append(scheduler.getCode())
               .append("-")
               .append(dateStr)
               .append(index != null ? "-" : "")
               .append(postfix);
    return codeBuilder.toString();
  }

  public static int getTotalIssuesByOpenDuration(Integer openDuration) {
    Assert.notNull(openDuration, "cannot getTotalIssuesByOpenDuration 'coz openDuration is null");
    int aDayInSecond = 24 * 60 * 60;
    return openDuration >= aDayInSecond ? 0 : aDayInSecond / openDuration;
  }

  public static boolean isValidDrawResult(Set<Prize> prizes, Set<DrawResult> results) {
    Assert.notEmpty(prizes, "cannot check isValidDrawResult 'coz prizes is empty");

    if (CollectionUtils.isEmpty(results)) return false;

    AtomicBoolean isValid = new AtomicBoolean(false);
    prizes.forEach(prize -> isValid.set(results.stream().anyMatch(
        result -> result.getPrize().getId().equals(prize.getId())
            && isValidWinNumbers(prize.getPrizeSchema(), result.getWinNo())
    )));

    return isValid.get();
  }

  static boolean isValidWinNumbers(PrizeSchema prize, String winNumbers) {
    Assert.notNull(prize, "cannot check isValidWinNumbers 'coz prize is null");
    if (StringUtils.isBlank(winNumbers)) return false;

    String[] winNumbersBreakdown = StringUtils.split(winNumbers, DrawResult.WIN_NO_DELIMITER);
    return prize.getWinNoSize() == winNumbersBreakdown.length
        && Arrays.stream(winNumbersBreakdown).allMatch(
        winNo -> StringUtils.isNotBlank(winNo)
            && StringUtils.trimToEmpty(winNo).length() == prize.getWinNoLength());
  }

  public LotteryIssueDTO buildLotteryIssueMsg(Issue issue, Set<DrawResult> drawResults) {
    Assert.notNull(issue, "cannot buildLotteryIssueMsg for null issue");

    return LotteryIssueDTO
        .builder()
        .id(String.valueOf(issue.getId()))
        .code(issue.getCode())
        .openingTime(issue.getActualOpeningTime() == null
                     ? issue.getOpeningTime()
                            .atZone(ZoneId.systemDefault())
                            .truncatedTo(ChronoUnit.SECONDS)
                            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                     : issue.getActualOpeningTime()
                            .atZone(ZoneId.systemDefault())
                            .truncatedTo(ChronoUnit.SECONDS)
                            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        .closingTime(issue.getActualClosingTime() == null
                     ? issue.getClosingTime()
                            .atZone(ZoneId.systemDefault())
                            .truncatedTo(ChronoUnit.SECONDS)
                            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                     : issue.getActualClosingTime()
                            .atZone(ZoneId.systemDefault())
                            .truncatedTo(ChronoUnit.SECONDS)
                            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        .endingTime(issue.getEndingTime() == null
                    ? null
                    : issue.getEndingTime()
                           .atZone(ZoneId.systemDefault())
                           .truncatedTo(ChronoUnit.SECONDS)
                           .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        .status(issue.getStatus().name())
        .results(drawResults == null || drawResults.isEmpty()
                 ? null
                 : LotteryUtil.toDrawResultsString(drawResults))
        .build();
  }

  public String toDrawResultsString(Set<DrawResult> drawResults) {
    return drawResults
        .stream()
        .sorted(Comparator.comparing(o -> o.getPrize().getPrizePosition()))
        .map(result -> StringUtils.joinWith(":", result.getPrize().getCode(), result.getWinNo()))
        .collect(Collectors.joining("; "));
  }

  static DrawResult reduceDrawResults(
      DrawResult drawResult1,
      DrawResult drawResult2
  ) {
    if (drawResult1 == null || StringUtils.isBlank(drawResult1.getWinNo())) return drawResult2;
    if (drawResult2 == null || StringUtils.isBlank(drawResult1.getWinNo())) return drawResult1;
    Assert.isTrue(
        Objects.equals(drawResult1.getPrize(), drawResult2.getPrize()),
        "prize of 2 draw results is not identical"
    );
    Assert.isTrue(
        Objects.equals(drawResult1.getIssueId(), drawResult2.getIssueId()),
        "issue of 2 draw results is not identical"
    );
    Assert.isTrue(
        Objects.equals(drawResult1.getWinNo(), drawResult2.getWinNo()),
        "win numbers of 2 draw results is not identical"
    );

    return drawResult1;
  }
}
