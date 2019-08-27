package com.a18.lottery.service;

import com.a18.lottery.model.DrawResult;
import com.a18.lottery.model.Issue;
import com.a18.lottery.model.Issue.IssueStatus;
import com.a18.lottery.model.Prize;
import com.a18.lottery.model.Scheduler;
import com.a18.lottery.model.Ticket;
import com.a18.lottery.model.repository.DrawResultRepository;
import com.a18.lottery.model.repository.PrizeRepository;
import com.a18.lottery.util.LotteryUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Common Procedure of a lottery, contains following steps:
 * <pre>
 *   - Issues all lotteries in current cycle (and may be next cycle) at fixed time, their status is NEW
 *   - Opens an issue when it's opening time has come, update it's status = OPENING
 *   - Closes issue: update it's status = CLOSED
 *   - if issue's been closed, attempt to end the issue:
 *    + gets draw results: if results already existed on DB then get them else generating by our shelf
 *      or getting from external sources depend on lottery's type
 *    + check and update win amt & ticket status
 *    + update issue status to ENDED
 *
 * </pre>
 */
@Slf4j
@Service
public abstract class CommonLotteryService {

  @Autowired @Lazy protected IssueService issueService;

  @Autowired @Lazy protected DrawResultRepository resultRepository;

  @Autowired @Lazy protected PrizeRepository prizeRepository;

  @Autowired @Lazy protected TicketResultCalculatingService ticketResultCalculatingService;

  @Retryable
  public abstract void issueLotteries(Scheduler scheduler, LocalDate issueDate);

  /**
   * <pre>
   *   - looking for a issue that should be opened at the time (expect only 1 per scheduler)
   *   - if not exists (due to in given scheduler is in idle duration), looking for next one
   *   - then do:
   *    + if it's status is {@link IssueStatus#NEW}, update to {@link IssueStatus#OPENING}
   *    + set actual opening time = now
   *    + push notification to FCM
   *    + create a issue cache for it
   * </pre>
   */
  @Retryable
  @Transactional
  public Optional<Issue> lookupAndOpenIssue(Integer schedulerId) {
    Assert.notNull(schedulerId, "cannot lookupAndOpenIssue for null schedulerId");

    return this.issueService
        .lookupCurrentIssue(schedulerId, Set.of(IssueStatus.OPENING, IssueStatus.NEW))
        .or(() -> this.issueService.lookUpNextIssue(
            schedulerId,
            Set.of(IssueStatus.OPENING, IssueStatus.NEW)
        ))
        .map(issue -> {
          this.issueService.updateIssueOnFirestore(
              this.issueService.updateIssueStatus(issue, IssueStatus.OPENING),
              Set.of(),
              true
          );
          return issue;
        });
  }

  @Transactional
  public void finishExpiredIssues(Set<Scheduler> schedulers) {
    schedulers.forEach(
        scheduler -> {
          this.issueService
              .getAllBySchedulerIdAndClosingTimeBeforeAndStatusIn(
                  scheduler.getId(),
                  LocalDateTime.now(),
                  Set.of(IssueStatus.OPENING, IssueStatus.CLOSED)
              )
              .forEach(issue -> {
                log.trace("finishExpiredIssues : {} - {}", issue.getId(), issue.getCode());
                this.finishIssue(scheduler, issue, false);
              });
          this.issueService
              .getAllBySchedulerIdAndClosingTimeBeforeAndStatusIn(
                  scheduler.getId(),
                  LocalDateTime.now(),
                  Set.of(IssueStatus.NEW)
              )
              .forEach(issue -> this.issueService.updateIssueStatus(issue, IssueStatus.ENDED));
        }
    );
  }

  /**
   * Try to close and end issue with given id:
   * <pre>
   * - first check if the issue has any tickets which in
   *   {@link Ticket.TicketStatus#NEW} or/and {@link Ticket.TicketStatus#PAYING} status
   * </pre>
   */
  @Transactional
  public void finishIssue(Scheduler scheduler, Issue issue, boolean onlyOnCache) {
    Assert.notNull(issue, "cannot finishIssue for null issueId");
    Set<Prize> prizes = this.prizeRepository.getAllBySchedulerIdAndStatusIn(
        issue.getSchedulerId(),
        Set.of(Prize.PrizeStatus.ACTIVE)
    );

    if (this.ticketResultCalculatingService.isAllTicketsSolved(issue.getId())) {
      this.issueService.updateIssueOnFirestore(
          this.issueService.updateIssueStatus(issue, IssueStatus.ENDED),
          Set.of(),
          false
      );
    } else {
      try {
        Set<DrawResult> drawResults = this.getDrawResults(scheduler, issue, prizes);
        if (LotteryUtil.isValidDrawResult(prizes, drawResults)
            && this.payout(issue, drawResults, onlyOnCache)) {
          this.issueService.updateIssueOnFirestore(
              this.issueService.updateIssueStatus(issue, IssueStatus.ENDED),
              drawResults,
              false
          );
        } else {
          this.issueService.updateIssueOnFirestore(
              this.issueService.updateIssueStatus(issue, IssueStatus.CLOSED),
              drawResults,
              false
          );
        }
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        this.issueService.updateIssueStatus(issue, IssueStatus.CLOSED);
      }
    }
  }

  @Transactional
  Set<DrawResult> getDrawResults(
      Scheduler scheduler,
      Issue issue,
      Set<Prize> prizes
  ) {
    Assert.notNull(issue, "cannot getDrawResults 'coz issue is null. issue: " + issue.getCode());
    Assert.notEmpty(
        prizes,
        "cannot getDrawResults 'coz scheduler has no prizes. . issue: " + issue.getCode()
    );

    Set<DrawResult> results = this.getExistedDrawResults(issue);
    if (!LotteryUtil.isValidDrawResult(prizes, results)) {
      this.resultRepository.deleteAll(results);
      results = this.getNewDrawResults(scheduler, issue, prizes);
      this.resultRepository.saveAll(results);
    }

    log.trace(
        "{} {} {}",
        String.format("%-10s", "RESULT"),
        String.format("%-20s", issue.getCode()),
        LotteryUtil.toDrawResultsString(results)
    );

    return results;
  }

  @Transactional
  boolean payout(Issue issue, Set<DrawResult> drawResults, boolean onlyOnCache) {
    Assert.notNull(issue, "cannot payout for null issue");

    if (!IssueStatus.OPENING.equals(issue.getStatus())
        && !IssueStatus.CLOSED.equals(issue.getStatus())) {
      return true;
    }

    log.trace(
        "{} {} {}",
        String.format("%-10s", "PAYOUT"),
        String.format("%5s", issue.getId()),
        String.format("%20s", issue.getCode())
    );

    this.ticketResultCalculatingService.calculateBettingResult(
        issue.getId(),
        drawResults,
        onlyOnCache
    );
    this.ticketResultCalculatingService.cleanExpiredIssueCachingTickets(issue.getId());

    return this.ticketResultCalculatingService.isAllTicketsSolved(issue.getId());
  }

  @Transactional
  Set<DrawResult> getExistedDrawResults(Issue issue) {
    Assert.notNull(issue, "cannot getExistedDrawResults for null issue");
    return Set.copyOf(this.resultRepository.getAllByIssueId(issue.getId()));
  }

  @Transactional
  abstract Set<DrawResult> getNewDrawResults(
      Scheduler scheduler,
      Issue issue,
      Set<Prize> prizes
  );
}
