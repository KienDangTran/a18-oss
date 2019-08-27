package com.a18.lottery.service;

import com.a18.common.constant.GameCategory;
import com.a18.common.constant.Journal;
import com.a18.common.constant.Privilege;
import com.a18.common.dto.JournalDTO;
import com.a18.common.util.AuthUtil;
import com.a18.lottery.model.Issue;
import com.a18.lottery.model.Ticket;
import com.a18.lottery.model.repository.TicketRepository;
import com.a18.lottery.stream.LotterySourceStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import javax.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import static com.a18.common.constant.RabbitMQExchange.LOTTERY_JOURNAL_EXCHANGE;

@Slf4j
@Component
@Lazy
public class LotteryJournalMessagesProducer {

  @Autowired @Lazy private LotterySourceStream lotterySourceStream;

  @Autowired @Lazy ObjectMapper objectMapper;

  @Autowired @Lazy private IssueService issueService;

  @Autowired @Lazy private TicketRepository ticketRepository;

  @Autowired @Lazy private AuthUtil authUtil;

  void sendLotteryFinalJournal(Ticket ticket) {
    boolean isJournalMsgSent = this.sendJournalMsg(
        ticket,
        Journal.BET,
        ticket.getTotalBetAmt(),
        JournalDTO.JournalStatus.FINAL
    );

    if (Objects.equals(ticket.getStatus(), Ticket.TicketStatus.WON)) {
      isJournalMsgSent &= this.sendJournalMsg(
          ticket,
          Journal.PAYOUT,
          ticket.getTotalPayout(),
          JournalDTO.JournalStatus.FINAL
      );
    }

    if (!isJournalMsgSent) {
      ticket.setStatus(Ticket.TicketStatus.PAYING);
    }
  }

  @Retryable public boolean sendJournalMsg(
      Ticket ticket,
      Journal journal,
      BigDecimal amt,
      JournalDTO.JournalStatus status
  ) {
    Assert.notNull(ticket, "cannot sendJournalMsg 'coz ticket is null");
    Assert.notNull(journal, "cannot sendJournalMsg 'coz journal is null");
    Assert.notNull(amt, "cannot sendJournalMsg 'coz amt is null");
    Assert.notNull(status, "cannot sendJournalMsg 'coz status is null");

    String deviceToken = this.authUtil.getUserDeviceRegistrationToken();
    if (StringUtils.isBlank(deviceToken)) {
      deviceToken = ticket.getDeviceRegistrationToken();
    }

    return this.lotterySourceStream
        .sendLotteryJournalMessage()
        .send(MessageBuilder.withPayload(
            JournalDTO
                .builder()
                .journal(journal)
                .ccy(ticket.getCcy())
                .gameCategory(GameCategory.LOTTERY)
                .amt(amt)
                .username(ticket.getUsername())
                .refId(ticket.getId())
                .refType(Ticket.class.getSimpleName().toUpperCase())
                .status(status)
                .registrationTokens(deviceToken)
                .build()
        ).build(), 30000);
  }

  @Transactional
  @RabbitListener(queues = "${spring.cloud.stream.rabbit.bindings."
      + LOTTERY_JOURNAL_EXCHANGE
      + ".producer.prefix}"
      + LOTTERY_JOURNAL_EXCHANGE
      + ".${spring.cloud.stream.default.producer.required-groups}.dlq")
  public void handleFailedJournalMsg(Message failedMessage) {
    try {
      JournalDTO journal =
          objectMapper.readValue((byte[]) failedMessage.getPayload(), JournalDTO.class);
      log.error("{} was failed: {}", journal);

      initSecurityContextIfEmpty();// FIXME
      Ticket ticket = this.ticketRepository.getOne(journal.getRefId());
      ticket.setStatus(Ticket.TicketStatus.PAYING);
      this.ticketRepository.save(ticket);

      this.issueService
          .findById(ticket.getIssueId())
          .ifPresent(issue -> this.issueService.updateIssueStatus(
              issue,
              Issue.IssueStatus.CLOSED
          ));
    } catch (EntityNotFoundException e) {
      log.error("Ref id of Failed journal message doesn't exists");
    } catch (Exception e) {
      log.error("", e);
    } finally {
      SecurityContextHolder.clearContext();
    }
  }

  private void initSecurityContextIfEmpty() {
    if (SecurityContextHolder.getContext().getAuthentication() == null) {
      SecurityContextHolder
          .getContext()
          .setAuthentication(new UsernamePasswordAuthenticationToken(
              "rabbit",
              null,
              List.of(
                  new SimpleGrantedAuthority(Privilege.read(Privilege.TICKET)),
                  new SimpleGrantedAuthority(Privilege.write(Privilege.TICKET)),
                  new SimpleGrantedAuthority(Privilege.read(Privilege.ISSUE)),
                  new SimpleGrantedAuthority(Privilege.write(Privilege.ISSUE))
              )
          ));
    }
  }
}
