package com.a18.lottery.service;

import com.a18.common.exception.ApiException;
import com.a18.lottery.model.BetItemGroup;
import com.a18.lottery.model.DrawResult;
import com.a18.lottery.model.Lottery;
import com.a18.lottery.model.Rule;
import com.a18.lottery.model.Ticket;
import com.a18.lottery.model.WonItem;
import com.a18.lottery.model.cache.CachingTicket;
import com.a18.lottery.model.dto.BetItemGroupDTO;
import com.a18.lottery.model.dto.TicketDTO;
import com.a18.lottery.model.repository.CachingTicketRepository;
import com.a18.lottery.model.repository.LotteryRepository;
import com.a18.lottery.model.repository.RuleRepository;
import com.a18.lottery.model.repository.TicketRepository;
import com.a18.lottery.model.repository.WonItemRepository;
import com.a18.lottery.util.LotterySchemaUtil;
import com.a18.lottery.util.TicketUtil;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
public class TicketResultCalculatingService {

  @Autowired @Lazy private TicketRepository ticketRepository;

  @Autowired @Lazy private WonItemRepository wonItemRepository;

  @Autowired @Lazy private LotteryRepository lotteryRepository;

  @Autowired @Lazy private RuleRepository ruleRepository;

  @Autowired @Lazy private CachingTicketRepository cachingTicketRepository;

  @Autowired @Lazy private LotteryJournalMessagesProducer lotteryJournalMessagesProducer;

  boolean isAllTicketsSolved(Long issueId) {
    Assert.notNull(issueId, "isAllTicketsSolved#issueId cannot be null");
    return this.cachingTicketRepository.findAllByIssueId(issueId).size() == 0
        && this.ticketRepository.countAllByIssueIdAndStatusIn(
        issueId,
        Set.of(Ticket.TicketStatus.NEW, Ticket.TicketStatus.PAYING)
    ) == 0;
  }

  @Transactional
  void calculateBettingResult(Long issueId, Set<DrawResult> drawResults, boolean onlyOnCache) {
    Assert.notNull(issueId, "cannot calculateBettingResult 'coz issueId is null");

    if (CollectionUtils.isEmpty(drawResults)) return;

    this.ticketRepository.saveAll(
        this.cachingTicketRepository
            .findAllByIssueId(issueId)
            .stream()
            .map(TicketDTO::new)
            .distinct()
            .map(ticketDTO -> this.updateTicketResult(ticketDTO, drawResults))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .peek(this.lotteryJournalMessagesProducer::sendLotteryFinalJournal)
            .collect(Collectors.toSet())
    );

    if (!onlyOnCache) {
      this.ticketRepository.saveAll(
          this.ticketRepository
              .getAllByIssueId(issueId)
              .map(TicketDTO::new)
              .distinct()
              .map(ticketDTO -> this.updateTicketResult(ticketDTO, drawResults))
              .filter(Optional::isPresent)
              .map(Optional::get)
              .peek(this.lotteryJournalMessagesProducer::sendLotteryFinalJournal)
              .collect(Collectors.toSet())
      );
    }
  }

  private Optional<Ticket> updateTicketResult(TicketDTO ticketDTO, Set<DrawResult> drawResults) {
    Lottery lottery = this.lotteryRepository
        .findById(ticketDTO.getLotteryId())
        .orElseThrow(() -> new ApiException("lottery.not.found"));

    Set<WonItem> wonItems = this.collectWonItems(ticketDTO, drawResults, lottery);
    Ticket ticket;
    try {
      ticket = this.ticketRepository.getOne(ticketDTO.getId());
    } catch (EntityNotFoundException ex) {
      return Optional.empty();
    }

    if (CollectionUtils.isEmpty(wonItems)) {
      ticket.setStatus(Ticket.TicketStatus.LOST);
    } else {
      this.wonItemRepository.saveAll(wonItems);

      int wonUnit = wonItems.stream().mapToInt(WonItem::getWonUnit).sum();
      BigDecimal maxPayout = LotterySchemaUtil.getLotteryMaxPayout(lottery);
      BigDecimal payout =
          LotterySchemaUtil.getLotteryWinUnitPrice(lottery).multiply(BigDecimal.valueOf(wonUnit));
      ticket.setTotalWonUnit(wonUnit);
      ticket.setTotalPayout(maxPayout != null ? maxPayout.min(payout) : payout);
      ticket.setStatus(Ticket.TicketStatus.WON);
    }

    log.trace(
        "\t {}: username={} \t  ticketId={} \t betUnit={} \t wonUnit={}",
        ticket.getStatus(),
        ticket.getUsername(),
        ticket.getId(),
        ticket.getTotalBetUnit(),
        ticket.getTotalWonUnit()
    );
    return Optional.of(ticket);
  }

  private Set<WonItem> collectWonItems(
      TicketDTO ticketDTO,
      Set<DrawResult> drawResults,
      Lottery lottery
  ) {
    Assert.notNull(ticketDTO, "cannot collectWonItems 'coz ticket is null");
    Assert.notNull(lottery, "cannot collectWonItems 'coz lottery is null");
    Assert.notEmpty(drawResults, "cannot collectWonItems 'coz rules is drawResults");

    Map<String, Integer> betItems = TicketUtil.collectBetItems(
        ticketDTO.getBetItemGroups()
                 .stream()
                 .collect(Collectors.toMap(
                     BetItemGroupDTO::getBetUnit,
                     BetItemGroupDTO::getBetItems,
                     (s, s2) -> StringUtils.joinWith(BetItemGroup.ITEM_CONTENTS_DELIMITER, s, s2)
                 ))
    );
    Set<Rule> rules = this.ruleRepository.findAllByLotterySchemaIdAndStatusIn(
        lottery.getLotterySchemaId(),
        Set.of(Rule.RuleStatus.ACTIVE)
    );

    return betItems
        .entrySet()
        .stream()
        .map(entry -> {
              int wonTime = TicketUtil.countWinTime(entry.getKey(), rules, drawResults);
              return new WonItem(
                  null,
                  ticketDTO.getId(),
                  entry.getKey(),
                  wonTime,
                  entry.getValue() * wonTime
              );
            }
        )
        .filter(entry -> NumberUtils.compare(entry.getWonUnit(), 0) > 0)
        .collect(Collectors.toSet());
  }

  void cleanExpiredIssueCachingTickets(Long issueId) {
    this.cachingTicketRepository
        .findAllByIssueId(issueId)
        .forEach(this::checkAndDeleteCachingTicket);
  }

  private void checkAndDeleteCachingTicket(CachingTicket cachingTicket) {
    if (!this.ticketRepository.existsByIssueIdAndLotteryIdAndCcyAndUsername(
        cachingTicket.getIssueId(),
        cachingTicket.getLotteryId(),
        cachingTicket.getCcy(),
        cachingTicket.getUsername()
    )) {
      this.cachingTicketRepository.delete(cachingTicket);
    }
  }
}
