package com.a18.lottery.service;

import com.a18.lottery.model.DrawResult;
import com.a18.lottery.model.Issue;
import com.a18.lottery.model.Prize;
import com.a18.lottery.model.Scheduler;
import com.a18.lottery.util.LotteryDrawResultFetcher;
import com.a18.lottery.util.LotteryUtil;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Slf4j
@Service
public class TraditionalLotteryService extends CommonLotteryService {

  public void issueLotteries(Scheduler scheduler, LocalDate issueDate) {
    Assert.notNull(scheduler, "cannot issue lotteries for null scheduler");
    Assert.notNull(issueDate, "cannot issue lotteries for null issueDate");
    if (this.issueService.isFullyIssued(
        this.getOpenDaysInWeek(scheduler).length,
        scheduler.getId(),
        issueDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.of(1)))
                 .atTime(scheduler.getStartAt()),
        issueDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.of(7)))
                 .atTime(scheduler.getStartAt())
    )) { return; }

    Set<Issue> allIssues = new HashSet<>();
    List<LocalDate> openDatesInCurrentWeek =
        this.getTraditionalLotteryOpenDatesInCurrentWeek(scheduler, issueDate)
            .stream()
            .distinct()
            .sorted()
            .collect(Collectors.toList());

    for (int i = 0; i < openDatesInCurrentWeek.size() - 1; i++) {
      LocalDate openingDate = openDatesInCurrentWeek.get(i);
      String code = LotteryUtil.buildLotteryIssueCode(scheduler, openingDate, null);
      if (this.issueService.countAllBySchedulerIdAndCode(scheduler.getId(), code) <= 0) {
        Issue issue = new Issue();
        issue.setCode(LotteryUtil.buildLotteryIssueCode(scheduler, openingDate, null));
        issue.setOpeningTime(openingDate.atTime(scheduler.getStartAt()
                                                         .truncatedTo(ChronoUnit.SECONDS)));
        issue.setClosingTime(openDatesInCurrentWeek.get(i + 1)
                                                   .atTime(scheduler.getStartAt())
                                                   .minusSeconds(scheduler.getIdleDuration())
                                                   .truncatedTo(ChronoUnit.SECONDS));
        issue.setSchedulerId(scheduler.getId());
        issue.setStatus(issue.getClosingTime().isBefore(LocalDateTime.now())
                        ? Issue.IssueStatus.ENDED
                        : Issue.IssueStatus.NEW);
        allIssues.add(issue);

        log.info(
            "ISSUED {} ({} -> {})",
            String.format("%18s", issue.getCode()),
            issue.getOpeningTime().format(DateTimeFormatter.ISO_DATE_TIME),
            issue.getClosingTime().format(DateTimeFormatter.ISO_DATE_TIME)
        );
      }
    }
    this.issueService.saveAll(allIssues);
  }

  private String[] getOpenDaysInWeek(Scheduler scheduler) {
    return StringUtils.split(scheduler.getOpenDay(), Scheduler.RSS_SRC_DELIMITER);
  }

  private List<LocalDate> getTraditionalLotteryOpenDatesInCurrentWeek(
      Scheduler scheduler,
      LocalDate issueDate
  ) {
    if (StringUtils.isBlank(scheduler.getOpenDay())) return List.of();

    List<LocalDate> openingDates = new ArrayList<>();

    Set<DayOfWeek> openDays = Arrays.stream(this.getOpenDaysInWeek(scheduler))
                                    .map(DayOfWeek::valueOf)
                                    .collect(Collectors.toSet());

    DayOfWeek firstOpenDayInWeek =
        openDays.stream().min(Comparator.comparingInt(DayOfWeek::getValue)).orElse(DayOfWeek.of(1));
    DayOfWeek lastOpenDayInWeek =
        openDays.stream().max(Comparator.comparingInt(DayOfWeek::getValue)).orElse(DayOfWeek.of(7));

    if (issueDate.getDayOfWeek().getValue() <= firstOpenDayInWeek.getValue()
        || issueDate.getDayOfWeek().getValue() >= lastOpenDayInWeek.getValue()) {
      openingDates.add(issueDate.with(TemporalAdjusters.previous(lastOpenDayInWeek)));
    }

    openDays.forEach(day -> {
      LocalDate date = issueDate.with(ChronoField.DAY_OF_WEEK, day.getValue());
      openingDates.add(date);
      if (day.equals(firstOpenDayInWeek)) {
        openingDates.add(date.plusDays(7));
      }
    });

    return openingDates;
  }

  @Override
  public Set<DrawResult> getNewDrawResults(Scheduler scheduler, Issue issue, Set<Prize> prizes) {
    return LotteryDrawResultFetcher.fetchAndConsolidateDrawResultsFromAllSources(
        scheduler,
        issue,
        prizes
    );
  }
}
