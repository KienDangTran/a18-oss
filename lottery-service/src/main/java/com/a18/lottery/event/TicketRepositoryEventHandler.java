package com.a18.lottery.event;

import com.a18.common.constant.Journal;
import com.a18.common.dto.JournalDTO;
import com.a18.common.exception.ApiException;
import com.a18.common.util.AuthUtil;
import com.a18.lottery.model.BetItemGroup;
import com.a18.lottery.model.Issue;
import com.a18.lottery.model.Lottery;
import com.a18.lottery.model.Ticket;
import com.a18.lottery.model.cache.CachingTicket;
import com.a18.lottery.model.dto.TicketDTO;
import com.a18.lottery.model.repository.CachingTicketRepository;
import com.a18.lottery.model.repository.LotteryRepository;
import com.a18.lottery.service.IssueService;
import com.a18.lottery.service.LotteryJournalMessagesProducer;
import com.a18.lottery.util.LotterySchemaUtil;
import com.a18.lottery.util.TicketUtil;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Lazy
@RepositoryEventHandler
public class TicketRepositoryEventHandler {
  @Autowired @Lazy private CachingTicketRepository cachingTicketRepository;

  @Autowired @Lazy private LotteryRepository lotteryRepository;

  @Autowired @Lazy private LotteryJournalMessagesProducer lotteryJournalMessagesProducer;

  @Autowired @Lazy private IssueService issueService;

  @Autowired @Lazy private AuthUtil authUtil;

  @HandleBeforeCreate
  @HandleBeforeSave
  void handleBeforeSaveOrCreate(Ticket ticket) {
    Lottery lottery = this.lotteryRepository.getOne(ticket.getLotteryId());
    this.checkAndFillRequiredProperties(ticket, lottery);
  }

  @HandleAfterSave
  @HandleAfterCreate
  void handleAfterSaveOrCreate(Ticket ticket) {
    if (!this.lotteryJournalMessagesProducer.sendJournalMsg(
        ticket,
        Journal.BET,
        ticket.getTotalBetAmt(),
        JournalDTO.JournalStatus.IN_PROGRESS
    )) {
      ticket.setStatus(Ticket.TicketStatus.FAILED);
      this.cachingTicketRepository.deleteById(ticket.getId());
    } else {
      Lottery lottery = this.lotteryRepository.getOne(ticket.getLotteryId());
      cacheTicket(ticket, lottery);
    }
  }

  @HandleAfterDelete
  void handleAfterDelete(Ticket ticket) {
    this.lotteryJournalMessagesProducer.sendJournalMsg(
        ticket,
        Journal.BET,
        ticket.getTotalBetAmt(),
        JournalDTO.JournalStatus.CANCELED
    );
  }

  private void checkAndFillRequiredProperties(Ticket ticket, Lottery lottery) {
    ticket.setUsername(SecurityContextHolder.getContext().getAuthentication().getName());
    ticket.setDeviceRegistrationToken(authUtil.getUserDeviceRegistrationToken());

    if (Objects.isNull(ticket.getIssueId())) {
      Issue issue = this.issueService
          .lookupCurrentIssue(
              lottery.getSchedulerId(),
              Set.of(Issue.IssueStatus.OPENING)
          )
          .or(() -> this.issueService.lookUpNextIssue(
              lottery.getSchedulerId(),
              Set.of(Issue.IssueStatus.NEW)
          ))
          .orElseThrow(() -> new ApiException("opening.issue.not.found"));
      ticket.setIssueId(issue.getId());
    }
    Map<String, Integer> betItems = TicketUtil.collectBetItems(
        ticket.getBetItemGroups()
              .stream()
              .collect(Collectors.toMap(
                  BetItemGroup::getBetUnit,
                  BetItemGroup::getBetItems,
                  (s, s2) -> StringUtils.joinWith(BetItemGroup.ITEM_CONTENTS_DELIMITER, s, s2)
              ))
    );
    ticket.setTotalBetItem(betItems.size());
    ticket.setTotalBetUnit(betItems.values().stream().reduce(0, Integer::sum));
    ticket.setTotalBetAmt(LotterySchemaUtil.getLotteryBetUnitPrice(lottery)
                                           .multiply(BigDecimal.valueOf(ticket.getTotalBetUnit())));
    ticket.getBetItemGroups().forEach(betItemGroup -> betItemGroup.setTicket(ticket));
  }

  private void cacheTicket(Ticket ticket, Lottery lottery) {
    this.cachingTicketRepository
        .findByIssueIdAndLotteryIdAndCcyAndUsername(
            ticket.getIssueId(),
            ticket.getLotteryId(),
            ticket.getCcy(),
            ticket.getUsername()
        )
        .ifPresentOrElse(
            existedCachingTicket -> {
              existedCachingTicket.setBetItemGroups(
                  ticket.getBetItemGroups()
                        .stream()
                        .collect(Collectors.toMap(
                            BetItemGroup::getBetUnit,
                            BetItemGroup::getBetItems,
                            (s, s2) ->
                                StringUtils.joinWith(BetItemGroup.ITEM_CONTENTS_DELIMITER, s, s2)
                        ))
              );
              this.cachingTicketRepository.save(existedCachingTicket);
            },
            () -> this.cachingTicketRepository.save(
                new CachingTicket(
                    new TicketDTO(ticket),
                    LotterySchemaUtil.getLotteryBetUnitPrice(lottery),
                    lottery.getLotterySchemaId()
                )
            )
        );
  }
}
