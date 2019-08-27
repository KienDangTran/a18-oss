package com.a18.lottery.service;

import com.a18.lottery.model.BetItemGroup;
import com.a18.lottery.model.DrawResult;
import com.a18.lottery.model.Issue;
import com.a18.lottery.model.Prize;
import com.a18.lottery.model.Rule;
import com.a18.lottery.model.Scheduler;
import com.a18.lottery.model.cache.CachingTicket;
import com.a18.lottery.model.repository.CachingTicketRepository;
import com.a18.lottery.model.repository.RuleRepository;
import com.a18.lottery.util.LotteryUtil;
import com.a18.lottery.util.QuickLotteryResultGenerator;
import com.a18.lottery.util.TicketUtil;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
public class QuickLotteryService extends CommonLotteryService {

  @Autowired @Lazy CachingTicketRepository cachingTicketRepository;

  @Autowired @Lazy private RuleRepository ruleRepository;

  @Override
  @Transactional
  public void issueLotteries(Scheduler scheduler, LocalDate issueDate) {
    Assert.notNull(scheduler, "cannot issue lottery for null scheduler");
    Assert.notNull(issueDate, "cannot issue lottery for null date");

    if (this.issueService.isFullyIssued(
        LotteryUtil.getTotalIssuesByOpenDuration(scheduler.getOpenDuration()),
        scheduler.getId(),
        issueDate.atTime(LocalTime.MIN),
        issueDate.atTime(LocalTime.MAX)
    )) { return; }

    Set<Issue> allIssues = new HashSet<>();
    int totalIssuePerDay = LotteryUtil.getTotalIssuesByOpenDuration(scheduler.getOpenDuration());
    IntStream.range(1, totalIssuePerDay + 1).forEach(index -> {
      String code = LotteryUtil.buildLotteryIssueCode(scheduler, issueDate, index);
      LocalDateTime openingTime =
          issueDate.atTime(LocalTime.MIN.plusSeconds(index * scheduler.getOpenDuration()));
      LocalDateTime closingTime =
          openingTime.plusSeconds(scheduler.getOpenDuration() - scheduler.getIdleDuration());

      if (this.issueService.countOverlapIssues(scheduler.getId(), openingTime) == 0
          && this.issueService.countAllBySchedulerIdAndCode(scheduler.getId(), code) == 0) {
        Issue issue = new Issue();
        issue.setCode(code);
        issue.setOpeningTime(openingTime);
        issue.setClosingTime(closingTime);
        issue.setSchedulerId(scheduler.getId());
        issue.setStatus(closingTime.isBefore(LocalDateTime.now())
                        ? Issue.IssueStatus.ENDED
                        : Issue.IssueStatus.NEW);
        allIssues.add(issue);
      }
    });

    this.issueService.saveAll(allIssues);

    log.info(
        "ISSUED {} {} {}",
        String.format("%5s", totalIssuePerDay),
        scheduler.getCode(),
        issueDate.format(DateTimeFormatter.ISO_DATE)
    );
  }

  @Override
  public Set<DrawResult> getNewDrawResults(Scheduler scheduler, Issue issue, Set<Prize> prizes) {
    Set<CachingTicket> cachedTickets = this.cachingTicketRepository.findAllByIssueId(issue.getId());
    if (CollectionUtils.isEmpty(cachedTickets)) {
      return drawRandomResults(issue, prizes);
    }

    return this.drawResultsBaseOnAmtOfBets(issue, prizes, cachedTickets);
  }

  private Set<DrawResult> drawRandomResults(Issue issue, Set<Prize> prizes) {
    return prizes
        .stream()
        .sorted(Comparator.comparingInt(Prize::getPrizePosition))
        .map(prize -> DrawResult
            .builder()
            .prize(prize)
            .prizeId(prize.getId())
            .issueId(issue.getId())
            .winNo(QuickLotteryResultGenerator.genAllWinNumbersRandomly(
                prize.getPrizeSchema().getWinNoSize(),
                prize.getPrizeSchema().getWinNoLength()
            ))
            .build()
        )
        .collect(Collectors.toSet());
  }

  private Set<DrawResult> drawResultsBaseOnAmtOfBets(
      Issue issue,
      Set<Prize> prizes,
      Set<CachingTicket> cachingTickets
  ) {
    return prizes
        .stream()
        .sorted(Comparator.comparingInt(Prize::getPrizePosition))
        .map(prize -> DrawResult
            .builder()
            .prize(prize)
            .prizeId(prize.getId())
            .issueId(issue.getId())
            .winNo(this.computeWinNumbersBaseOnAmtOfBets(prize, cachingTickets))
            .build())
        .collect(Collectors.toSet());
  }

  private String computeWinNumbersBaseOnAmtOfBets(Prize prize, Set<CachingTicket> cachingTickets) {
    Set<Rule> rules =
        this.ruleRepository.findAllByPrizeSchemasIdAndLotterySchemaIdInAndStatusIn(
            prize.getPrizeSchema().getId(),
            cachingTickets.stream()
                          .map(CachingTicket::getLotteryId)
                          .collect(Collectors.toUnmodifiableSet()),
            Set.of(Rule.RuleStatus.ACTIVE)
        );

    Map<String, BigDecimal> betContentsWithPotentialWinningAmt =
        this.calcPotentialWinningAmtForEachBetContent(
            cachingTickets,
            rules.stream().map(Rule::getLotterySchemaId).collect(Collectors.toUnmodifiableSet())
        );
    log.debug("{}", prize.getCode());
    return QuickLotteryResultGenerator.genWinNumbersBaseOnWeightOfBetContents(
        prize.getPrizeSchema().getWinNoSize(),
        prize.getPrizeSchema().getWinNoLength(),
        this.extractBetContentToIndividualBetNumbers(betContentsWithPotentialWinningAmt),
        rules
    );
  }

  private Map<String, BigDecimal> extractBetContentToIndividualBetNumbers(
      Map<String, BigDecimal> betContentsWithPotentialWinningAmt
  ) {
    return betContentsWithPotentialWinningAmt
        .entrySet()
        .stream()
        .map(entry ->
            Arrays.stream(StringUtils.split(entry.getKey(), BetItemGroup.BET_NUMBERS_DELIMITER))
                  .map(betNo -> new AbstractMap.SimpleEntry<>(betNo, entry.getValue()))
        )
        .flatMap(Stream::distinct)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, BigDecimal::add));
  }

  private Map<String, BigDecimal> calcPotentialWinningAmtForEachBetContent(
      Set<CachingTicket> cachingTickets,
      Set<Integer> lotterySchemaIds
  ) {
    return cachingTickets
        .stream()
        .filter(ticket -> lotterySchemaIds.contains(ticket.getLotterySchemaId()))
        .map(cachingTicket -> TicketUtil
            .collectBetItems(cachingTicket.getBetItemGroups())
            .entrySet()
            .stream()
            .map(entry -> new AbstractMap.SimpleEntry<>(
                entry.getKey(),
                cachingTicket.getBetUnitPrice().multiply(BigDecimal.valueOf(entry.getValue()))
            ))
        )
        .flatMap(Function.identity())
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, BigDecimal::add));
  }
}
