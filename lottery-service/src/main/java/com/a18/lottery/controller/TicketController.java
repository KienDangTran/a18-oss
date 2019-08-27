package com.a18.lottery.controller;

import com.a18.common.constant.Ccy;
import com.a18.common.constant.Privilege;
import com.a18.common.util.ResourceAssemblerHelper;
import com.a18.lottery.model.Ticket;
import com.a18.lottery.model.repository.TicketRepository;
import com.a18.lottery.specification.TicketSpecification;
import java.time.LocalDateTime;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RepositoryRestController
public class TicketController {
  private static final String PATH_TICKETS = "/tickets";

  @Autowired @Lazy private TicketRepository ticketRepository;

  @Autowired @Lazy private ResourceAssemblerHelper resourceAssemblerHelper;

  @Autowired @Lazy private PagedResourcesAssembler<Ticket> pagedAssembler;

  @PreAuthorize("hasAuthority('" + Privilege.READ + Privilege.TICKET + "') "
                    + " or #username.equals(authentication.name)")
  @GetMapping(PATH_TICKETS)
  public ResponseEntity findBySpec(
      @RequestParam(required = false) Long[] ids,
      @RequestParam(required = false) Integer[] schedulerIds,
      @RequestParam(required = false) Integer[] lotterySchemaIds,
      @RequestParam(required = false) Integer[] issueIds,
      @RequestParam(required = false) Ticket.TicketStatus[] statuses,
      @RequestParam(required = false) Ccy ccy,
      @RequestParam(required = false) String username,
      @RequestParam(name = "createdDate_gte", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdDateGte,
      @RequestParam(name = "createdDate_lte", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdDateLte,
      @PageableDefault Pageable pageable
  ) {
    return ResponseEntity.ok(this.pagedAssembler.toResource(
        this.ticketRepository.findAll(
            TicketSpecification.filterTicket(
                ids,
                issueIds,
                schedulerIds,
                lotterySchemaIds,
                statuses,
                ccy,
                username,
                createdDateGte,
                createdDateLte
            ),
            pageable
        ),
        resourceAssemblerHelper.resourceAssembler()
    ));
  }

  @GetMapping(PATH_TICKETS + "/currentUser")
  public ResponseEntity getAllCurrentUserTickets(
      @RequestParam(required = false) Long[] ids,
      @RequestParam(required = false) Integer[] schedulerIds,
      @RequestParam(required = false) Integer[] lotterySchemaIds,
      @RequestParam(required = false) Integer[] issueIds,
      @RequestParam(required = false) Ticket.TicketStatus[] statuses,
      @RequestParam(required = false) Ccy ccy,
      @RequestParam(name = "createdDate_gte", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdDateGte,
      @RequestParam(name = "createdDate_lte", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdDateLte,
      Authentication authentication,
      @PageableDefault Pageable pageable
  ) {
    if (authentication == null || StringUtils.isEmpty(authentication.getName())) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    return ResponseEntity.ok(this.pagedAssembler.toResource(
        this.ticketRepository.findAll(
            TicketSpecification.filterTicket(
                ids,
                issueIds,
                schedulerIds,
                lotterySchemaIds,
                statuses,
                ccy,
                StringUtils.trimToNull(authentication.getName()),
                createdDateGte,
                createdDateLte
            ),
            pageable
        ),
        resourceAssemblerHelper.resourceAssembler()
    ));
  }
}
