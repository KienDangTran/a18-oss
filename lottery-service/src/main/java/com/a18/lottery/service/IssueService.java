package com.a18.lottery.service;

import com.a18.common.constant.GameCategory;
import com.a18.common.dto.LotteryIssueDTO;
import com.a18.common.firebase.FirestoreUtils;
import com.a18.lottery.model.DrawResult;
import com.a18.lottery.model.Issue;
import com.a18.lottery.model.repository.IssueRepository;
import com.a18.lottery.util.LotteryUtil;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import javax.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Slf4j
@Service
public class IssueService {
  @Autowired @Lazy private IssueRepository issueRepository;

  @Autowired @Lazy private FirestoreUtils firestoreUtils;

  public Optional<Issue> lookupCurrentIssue(Integer schedulerId, Set<Issue.IssueStatus> statuses) {
    return this.issueRepository
        .findIssueByTimeAndStatus(
            schedulerId,
            LocalDateTime.now(),
            statuses
        );
  }

  public Optional<Issue> lookUpNextIssue(Integer schedulerId, Set<Issue.IssueStatus> statuses) {
    Assert.notNull(schedulerId, "cannot lookUpNextIssue for null schedulerId");

    return this.issueRepository
        .getTopBySchedulerIdAndOpeningTimeGreaterThanEqualAndStatusInOrderByOpeningTimeAsc(
            schedulerId,
            LocalDateTime.now(),
            statuses
        );
  }

  boolean isFullyIssued(int maxIssues, Integer schedulerId, LocalDateTime from, LocalDateTime to) {
    Assert.isTrue(
        schedulerId != null && from != null && to != null && from.compareTo(to) <= 0,
        "params invalid: schedulerId: " + schedulerId + ", from: " + from + ", to: " + to
    );
    return this.issueRepository.countAllBySchedulerIdAndOpeningTimeBetween(schedulerId, from, to)
        >= maxIssues;
  }

  public Optional<Issue> findById(Long issueId) {
    try {
      return Optional.of(this.issueRepository.getOne(issueId));
    } catch (EntityNotFoundException e) {
      return Optional.empty();
    }
  }

  Issue updateIssueStatus(Issue issue, Issue.IssueStatus status) {
    Assert.notNull(issue, "cannot updateIssueStatus for null issue");
    if (status.equals(issue.getStatus())) return issue;

    switch (status) {
      case OPENING:
        issue.setActualOpeningTime(LocalDateTime.now());
        break;
      case CLOSED:
        issue.setActualClosingTime(LocalDateTime.now());
        break;
      case ENDED:
        issue.setEndingTime(LocalDateTime.now());
        break;
    }

    issue.setStatus(status);

    log.trace(
        "{} {} {}",
        String.format("%-10s", status),
        String.format("%5s", issue.getId()),
        String.format("%20s", issue.getCode())
    );

    return this.issueRepository.save(issue);
  }

  Set<Issue> getAllBySchedulerIdAndClosingTimeBeforeAndStatusIn(
      Integer schedulerId,
      LocalDateTime time,
      Set<Issue.IssueStatus> statuses
  ) {
    return issueRepository.getAllBySchedulerIdAndClosingTimeBeforeAndStatusIn(
        schedulerId,
        time,
        statuses
    );
  }

  int countOverlapIssues(Integer schedulerId, LocalDateTime time) {
    return this.issueRepository.countOverlapIssues(schedulerId, time);
  }

  int countAllBySchedulerIdAndCode(Integer schedulerId, String code) {
    return this.issueRepository.countAllBySchedulerIdAndCode(schedulerId, code);
  }

  void saveAll(Set<Issue> allIssues) {
    this.issueRepository.saveAll(allIssues);
  }

  void updateIssueOnFirestore(Issue issue, Set<DrawResult> drawResults, boolean isCurrent) {
    Assert.notNull(issue, "cannot notifySubscribedUsers for null issue. issue: " + issue.getCode());

    LotteryIssueDTO msg = LotteryUtil.buildLotteryIssueMsg(issue, drawResults);
    this.firestoreUtils.addData(
        msg,
        GameCategory.LOTTERY.name().toLowerCase(),
        "issue",
        issue.getSchedulerId().toString(),
        isCurrent ? "current" : "previous"
    );
  }
}
