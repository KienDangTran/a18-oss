package com.a18.lottery.util;

import com.a18.lottery.model.DrawResult;
import com.a18.lottery.model.Issue;
import com.a18.lottery.model.Prize;
import com.a18.lottery.model.Scheduler;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

@Slf4j
@UtilityClass
public class LotteryDrawResultFetcher {

  // will match dd-MM dd/MM dd.MM
  private static final Pattern DATE_MONTH_PATTERN = Pattern.compile(
      "(?:(?:31([/\\-.])(?:0[13578]|10|12))|(?:(?:29|30)([/\\-.])(?:0[1,3-9]|10|11|12)))"
          + "|(?:29([/\\-.])02)"
          + "|(?:0?[1-9]|1\\d|2[0-8])([/\\-.])(?:(?:0[1-9])|(10|11|12))"
  );

  public static Set<DrawResult> fetchAndConsolidateDrawResultsFromAllSources(
      Scheduler scheduler,
      Issue issue,
      Set<Prize> prizes
  ) {
    Assert.notNull(
        issue,
        "cannot fetchAndConsolidateDrawResultsFromAllSources 'coz issue is null. issue: "
            + issue.getCode()
    );
    Assert.notNull(
        scheduler,
        "cannot fetchAndConsolidateDrawResultsFromAllSources 'coz issue's  scheduler is null. issue: "
            + issue.getCode()
    );
    Assert.isTrue(
        StringUtils.isNotBlank(scheduler.getDrawResultSrc()),
        "scheduler " + scheduler.getCode() + " has no draw result sources"
    );
    Assert.isTrue(
        prizes != null && prizes.size() != 0,
        "scheduler " + scheduler.getCode() + " has no prizes"
    );

    String[] rssSources =
        StringUtils.split(scheduler.getDrawResultSrc(), Scheduler.RSS_SRC_DELIMITER);

    return Set.copyOf(Stream
        .of(rssSources)
        .map(rssURL -> fetchAndExtractDrawResultsFromSingleSource(
            rssURL,
            issue.getClosingTime()
        ))
        .filter(StringUtils::isNotBlank)
        .map(winNumbers -> prizes
            .stream()
            .map(prize -> DrawResult.builder()
                                    .prize(prize)
                                    .prizeId(prize.getId())
                                    .issueId(issue.getId())
                                    .winNo(determineCorrespondingWinNumbersByPrizePosition(
                                        prize,
                                        breakdownWinNumbers(winNumbers)
                                    ).orElse(""))
                                    .build()
            ))
        .flatMap(Stream::distinct)
        .collect(Collectors.groupingBy(
            DrawResult::getPrize,
            Collectors.reducing(null, LotteryUtil::reduceDrawResults)
        ))
        .values()
    );
  }

  private static String fetchAndExtractDrawResultsFromSingleSource(
      String rssURL,
      LocalDateTime closingTime
  ) {
    Assert.isTrue(
        closingTime != null && !closingTime.isAfter(LocalDateTime.now()),
        "issue has no closing time or closing time ("
            + closingTime
            + ") is after current system time"
    );

    return readRss(rssURL)
        .map(syndFeed -> syndFeed
            .getEntries()
            .stream()
            .filter(e -> extractEntryDate(e.getTitle())
                .orElse(LocalDate.MIN)
                .compareTo(closingTime.toLocalDate()) == 0)
            .findFirst()
            .map(syndEntry -> StringUtils.trimToEmpty(syndEntry.getDescription().getValue()))
            .orElse(""))
        .orElse("");
  }

  private Optional<String> determineCorrespondingWinNumbersByPrizePosition(
      Prize prize,
      String[] allWinNumbers
  ) {
    if (allWinNumbers.length < prize.getPrizePosition() - 1) return Optional.empty();

    String winNumberOfGivenPrize = allWinNumbers[prize.getPrizePosition() - 1];
    if (StringUtils.isNotBlank(winNumberOfGivenPrize)
        && LotteryUtil.isValidWinNumbers(prize.getPrizeSchema(), winNumberOfGivenPrize)) {
      return Optional.of(StringUtils.trimToEmpty(winNumberOfGivenPrize));
    }
    return Optional.empty();
  }

  private static Optional<SyndFeed> readRss(String rssURL) {
    try {
      return Optional.of(new SyndFeedInput().build(new XmlReader(new URL(rssURL))));
    } catch (IOException | FeedException e) {
      log.error("can not read a feed from \"{}\". {}", rssURL, e.getMessage());
    }

    return Optional.empty();
  }

  /**
   * Parse the given title, looking for the date pattern and build a {@link LocalDate}.<br/> title
   * ex.: "KẾT QUẢ XỔ SỐ ĐỒNG THÁP NGÀY 30/07 (Thứ Hai)"
   */
  private static Optional<LocalDate> extractEntryDate(String rssTitle) {
    Matcher matcher = DATE_MONTH_PATTERN.matcher(rssTitle);

    if (!matcher.find()) {
      return Optional.empty();
    }

    String[] dayMonth = matcher.group(0).split("[/\\-.]");
    int day = Integer.parseInt(dayMonth[0]);
    int month = Integer.parseInt(dayMonth[1]);
    YearMonth thisYear = YearMonth.now();
    LocalDate date = LocalDate.of(thisYear.getYear(), month, day);
    return Optional.of(date);
  }

  /**
   * result text ex.:
   * <pre>
   * ĐB: 517112 1: 01068 2: 03700 3: 24224 - 63652 4: 04002 - 15226 - 23721 - 34060 - 16876 - 67057 - 92264 5: 1985 6: 1202 - 9727 - 0834 7: 3888: 90
   * </pre>
   */
  private static String[] breakdownWinNumbers(String rssResult) {
    String[] resultsBreakdown = fixRssBug(rssResult).split("\\n");
    List<String> winNumbers = new ArrayList<>();
    for (String s : resultsBreakdown) {
      String[] winNoWithPrize = StringUtils.split(s, ":");
      if (winNoWithPrize.length == 2 && StringUtils.isNotBlank(winNoWithPrize[1])) {
        winNumbers.add(StringUtils.trimToEmpty(winNoWithPrize[1]));
      }
    }
    return winNumbers.toArray(new String[] {});
  }

  /**
   * RSS provider has a bug that make RSS for some station return incorrectly for 8th prize. We have
   * to check and correct the rss result.
   */
  private static String fixRssBug(String rss) {
    final String bugPattern = "8: ";
    if (!rss.contains(bugPattern)) {
      return rss;
    }

    return rss.replace(bugPattern, "\n" + bugPattern);
  }
}
