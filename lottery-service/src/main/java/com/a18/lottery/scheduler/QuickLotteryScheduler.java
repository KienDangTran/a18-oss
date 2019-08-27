package com.a18.lottery.scheduler;

import com.a18.lottery.model.Issue;
import com.a18.lottery.model.LotteryCategory;
import com.a18.lottery.model.Scheduler;
import com.a18.lottery.model.repository.SchedulerRepository;
import com.a18.lottery.service.QuickLotteryService;
import com.a18.lottery.util.SchedulerUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Slf4j
@Component
public class QuickLotteryScheduler {
  private final ThreadPoolTaskScheduler threadPoolTaskScheduler;

  private final QuickLotteryService lotteryService;

  private final Set<Scheduler> quickLotteryScheduler;

  @Autowired @Lazy public QuickLotteryScheduler(
      @Qualifier("threadPoolTaskScheduler") ThreadPoolTaskScheduler threadPoolTaskScheduler,
      SchedulerRepository schedulerRepository,
      QuickLotteryService lotteryService
  ) {
    this.threadPoolTaskScheduler = threadPoolTaskScheduler;
    this.lotteryService = lotteryService;
    this.quickLotteryScheduler = schedulerRepository.findAllByCategoryInAndStatusIn(
        Set.of(LotteryCategory.QUICK),
        Set.of(Scheduler.SchedulerStatus.ACTIVE)
    );
  }

  public void init() {
    this.initLotteriesFor1stRun();
    this.initSchedulers();
  }

  /**
   * initial run for 1st time:
   * <pre>
   *   - issue all lotteries for today & tomorrow
   *   - re-open current lottery issue
   *    + look up a issue that suppose to be opened for now.
   *    + specify closing time = opening time + opening duration
   *   - cancel all expired lottery issue
   * </pre>
   */
  private void initLotteriesFor1stRun() {
    this.quickLotteryScheduler.forEach(scheduler -> {
      // issues lotteries if needed for today and tomorrow
      LocalDate today = LocalDate.now();
      this.lotteryService.issueLotteries(scheduler, today);
      this.lotteryService.issueLotteries(scheduler, today.plusDays(1));

      // re-opens opening lotteries if any
      this.threadPoolTaskScheduler.execute(
          () -> this.lotteryService
              .lookupAndOpenIssue(scheduler.getId())
              .ifPresent(issue -> this.scheduleCloseIssue(scheduler, issue))
      );
    });
  }

  /**
   * initial 2 different independent kind of schedulers, each kind of quick lottery will contain all
   * kind of schedulers
   * <pre>
   * - schedulers for issuing all quick lotteries for next 2 days at 12.00 AM everyday
   * - scheduler for opening issues at pre-determined time
   * </pre>
   */
  private void initSchedulers() {
    this.quickLotteryScheduler.forEach(scheduler -> {
      this.scheduleIssueQuickLotteries(scheduler);
      this.scheduleOpenIssue(scheduler);
    });
    this.scheduleCleanExpiredIssue();
  }

  private void scheduleCleanExpiredIssue() {
    this.threadPoolTaskScheduler.schedule(
        () -> this.lotteryService.finishExpiredIssues(this.quickLotteryScheduler),
        new CronTrigger("33 0/7 * * * *")
    );
  }

  /**
   * Issue lotteries and clean all expired issues for tmr at 12.AM everyday
   */
  private void scheduleIssueQuickLotteries(Scheduler scheduler) {
    Assert.notNull(scheduler, "cannot issue lotteries for null scheduler");
    this.threadPoolTaskScheduler.schedule(
        () -> this.lotteryService.issueLotteries(scheduler, LocalDate.now().plusDays(1)),
        new CronTrigger("0 0 0 * * *")
    );
    log.info(
        "quick lottery {} has been scheduling to issue at 12.AM everyday",
        String.format("%-5s", scheduler.getCode())
    );
  }

  /**
   * scheduler for opening issues
   * <pre>
   *   - look up a issue that suppose to be opened for now, if existed:
   *      + open issue
   *      + schedule close issue with it's designed closing time
   *      ({@link #scheduleCloseQuickLottery(Scheduler, Issue, LocalDateTime)})
   * </pre>
   */
  private void scheduleOpenIssue(Scheduler scheduler) {
    Assert.notNull(scheduler, "cannot schedule opening issue for null scheduler");

    this.threadPoolTaskScheduler.schedule(
        () -> this.lotteryService
            .lookupAndOpenIssue(scheduler.getId())
            .ifPresent(issue -> this.scheduleCloseIssue(scheduler, issue)),
        triggerContext -> Date.from(SchedulerUtil
            .computeQuickLotteryNextOpeningTime(LocalDateTime.now(), scheduler.getOpenDuration())
            .atZone(ZoneId.systemDefault())
            .toInstant()
        )
    );
  }

  private void scheduleCloseIssue(Scheduler scheduler, Issue issue) {
    Assert.notNull(issue, "cannot schedule closing issue for null issue ");

    this.threadPoolTaskScheduler.schedule(
        () -> this.lotteryService.finishIssue(scheduler, issue, true),
        SchedulerUtil.buildCronTriggerAtSpecificTime(issue.getClosingTime())
    );
  }
}
