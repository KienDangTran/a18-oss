package com.a18.lottery.scheduler;

import com.a18.lottery.model.Issue;
import com.a18.lottery.model.LotteryCategory;
import com.a18.lottery.model.Scheduler;
import com.a18.lottery.model.repository.SchedulerRepository;
import com.a18.lottery.service.TraditionalLotteryService;
import com.a18.lottery.util.SchedulerUtil;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
public class TraditionalLotteryScheduler {

  private final ThreadPoolTaskScheduler threadPoolTaskScheduler;

  private final TraditionalLotteryService lotteryService;

  private final Set<Scheduler> schedulers;

  @Autowired @Lazy public TraditionalLotteryScheduler(
      @Qualifier("threadPoolTaskScheduler") ThreadPoolTaskScheduler threadPoolTaskScheduler,
      SchedulerRepository schedulerRepository,
      TraditionalLotteryService lotteryService
  ) {
    this.threadPoolTaskScheduler = threadPoolTaskScheduler;
    this.lotteryService = lotteryService;
    this.schedulers = schedulerRepository.findAllByCategoryInAndStatusIn(
        Set.of(LotteryCategory.NORTH, LotteryCategory.MIDDLE, LotteryCategory.SOUTH),
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
   *   - issue all traditional lotteries for this week
   *   - re-open current lottery issue
   *    + look up a issue that suppose to be opened for now.
   *    + specify closing time = opening time + opening duration
   *   - cancel all expired lottery issue
   * </pre>
   */
  private void initLotteriesFor1stRun() {
    this.schedulers.forEach(scheduler -> {
      // issues lotteries if needed for THIS WEEK
      LocalDate today = LocalDate.now();
      this.lotteryService.issueLotteries(scheduler, today);

      // re-opens opening lotteries
      this.threadPoolTaskScheduler.execute(() -> this.lotteryService
          .lookupAndOpenIssue(scheduler.getId())
          .ifPresent(issue -> this.scheduleCloseCurrentIssue(scheduler, issue))
      );
    });
  }

  private void initSchedulers() {
    this.schedulers.forEach(this::initIssuingScheduler);
    this.scheduleCleanExpiredIssue();
  }

  private void scheduleCleanExpiredIssue() {
    this.threadPoolTaskScheduler.schedule(
        () -> this.lotteryService.finishExpiredIssues(this.schedulers),
        new CronTrigger("33 0/13 * * * *")
    );
  }

  /**
   * issue lottery for new week at 12.00 AM every Monday
   */
  private void initIssuingScheduler(Scheduler scheduler) {
    Assert.notNull(scheduler, "cannot issue lotteries for null scheduler");
    this.threadPoolTaskScheduler.schedule(
        () -> this.lotteryService.issueLotteries(scheduler, LocalDate.now().plusDays(1)),
        new CronTrigger("0 0 0 * * 1")
    );
    log.info(
        "traditional lottery {} has been scheduling to issue at 12.AM every Monday",
        String.format("%12s", scheduler.getCode())
    );
  }

  /**
   * <pre>
   * - closing time will be determined by opening time + opening duration
   * - try to close current issue
   * - specify next issue opening time = current issue's closing time + idle duration
   * </pre>
   */
  private void scheduleCloseCurrentIssue(Scheduler scheduler, Issue currentIssue) {
    Assert.notNull(currentIssue, "cannot schedule closing issue for null issue ");
    this.threadPoolTaskScheduler.schedule(
        () -> {
          this.lotteryService.finishIssue(scheduler, currentIssue, true);
          this.scheduleOpenNextIssue(scheduler, currentIssue);
        },
        SchedulerUtil.buildCronTriggerAtSpecificTime(currentIssue.getClosingTime())
    );
    log.trace(
        "{} will be closed at {}",
        String.format("%-18s", currentIssue.getCode()),
        currentIssue.getClosingTime()
    );
  }

  /**
   * schedules lottery next opening = currentIssue's closing time + idle duration
   */
  private void scheduleOpenNextIssue(Scheduler scheduler, Issue currentIssue) {
    Assert.notNull(
        scheduler,
        "cannot schedule opening issue for null scheduler and/or null issue "
    );
    this.threadPoolTaskScheduler.schedule(
        () -> this.lotteryService
            .lookupAndOpenIssue(scheduler.getId())
            .ifPresent(nextIssue -> this.scheduleCloseCurrentIssue(scheduler, nextIssue)),
        SchedulerUtil.buildCronTriggerAtSpecificTime(
            currentIssue.getClosingTime()
                        .plusSeconds(scheduler.getIdleDuration())
                        .truncatedTo(ChronoUnit.SECONDS)
        )
    );
    log.trace(
        "{} will be opened at {}",
        String.format("%20s", currentIssue.getCode()),
        currentIssue.getClosingTime()
    );
  }
}
