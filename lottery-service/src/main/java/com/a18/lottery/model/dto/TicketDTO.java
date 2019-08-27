package com.a18.lottery.model.dto;

import com.a18.common.constant.Ccy;
import com.a18.lottery.model.Ticket;
import com.a18.lottery.model.cache.CachingTicket;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Wither;

@Getter
@AllArgsConstructor
@EqualsAndHashCode(of = {"issueId", "lotteryId", "ccy", "username"})
@Wither
public class TicketDTO {
  public TicketDTO(Ticket ticket) {
    this.id = ticket.getId();
    this.issueId = ticket.getIssueId();
    this.lotteryId = ticket.getLotteryId();
    this.ccy = ticket.getCcy();
    this.username = ticket.getUsername();
    this.betItemGroups =
        ticket.getBetItemGroups()
              .stream()
              .map(BetItemGroupDTO::new)
              .collect(Collectors.toUnmodifiableSet());
  }

  public TicketDTO(CachingTicket cachingTicket) {
    this.id = cachingTicket.getId();
    this.issueId = cachingTicket.getIssueId();
    this.lotteryId = cachingTicket.getLotteryId();
    this.ccy = cachingTicket.getCcy();
    this.username = cachingTicket.getUsername();
    this.betItemGroups =
        cachingTicket.getBetItemGroups()
                     .entrySet()
                     .stream()
                     .map(entry -> new BetItemGroupDTO(entry.getKey(), entry.getValue(), Set.of()))
                     .collect(Collectors.toUnmodifiableSet());
  }

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private final Long id;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private final Long issueId;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private final String username;

  private final Integer lotteryId;

  private final Ccy ccy;

  private final Set<BetItemGroupDTO> betItemGroups;
}
