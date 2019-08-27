package com.a18.account.controller;

import com.a18.account.model.JournalEntry;
import com.a18.account.model.repository.JournalEntryRepository;
import com.a18.account.service.BalanceService;
import com.a18.account.specification.JournalEntrySpecification;
import com.a18.account.validator.JournalValidator;
import com.a18.common.constant.Ccy;
import com.a18.common.constant.GameCategory;
import com.a18.common.constant.Journal;
import com.a18.common.constant.Privilege;
import com.a18.common.dto.JournalDTO;
import com.a18.common.util.ResourceAssemblerHelper;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.core.RepositoryConstraintViolationException;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import static com.a18.common.constant.RabbitMQExchange.LOTTERY_JOURNAL_EXCHANGE;
import static com.a18.common.constant.RabbitMQExchange.PAYMENT_JOURNAL_EXCHANGE;

@RepositoryRestController
public class JournalEntryController {
  private static final String PATH_JOURNAL_ENTRIES = "/journalEntries";

  @Autowired @Lazy private BalanceService balanceService;

  @Autowired @Lazy private JournalValidator journalValidator;

  @Autowired @Lazy private ResourceAssemblerHelper resourceAssemblerHelper;

  @Autowired @Lazy private PagedResourcesAssembler<JournalEntry> pagedAssembler;

  @Autowired @Lazy private JournalEntryRepository journalEntryRepository;

  @InitBinder("journalDTO")
  public void setupBinder(WebDataBinder binder) {
    binder.addValidators(journalValidator);
  }

  @PreAuthorize("hasAuthority('" + Privilege.WRITE + Privilege.ACCOUNT + "')")
  @PostMapping(value = PATH_JOURNAL_ENTRIES)
  public ResponseEntity createJournal(
      @Valid @RequestBody JournalDTO journalDTO,
      BindingResult bindingResult
  ) {
    if (bindingResult.hasErrors()) throw new RepositoryConstraintViolationException(bindingResult);
    List<JournalEntry> content = this.balanceService.updateBalancesFromJournal(journalDTO);
    PagedResources<?> resource = this.pagedAssembler.toResource(
        new PageImpl<>(content, PageRequest.of(0, content.size()), 1),
        resourceAssemblerHelper.resourceAssembler()
    );

    return ResponseEntity.ok(resource);
  }

  @StreamListener(PAYMENT_JOURNAL_EXCHANGE)
  public void listenPaymentJournalMsg(@Payload JournalDTO journalDTO) {
    initSecurityContextIfEmpty();
    this.balanceService.updateBalancesFromJournal(journalDTO);
  }

  @StreamListener(LOTTERY_JOURNAL_EXCHANGE)
  public void listenLotteryJournalMsg(@Payload JournalDTO journalDTO) {
    initSecurityContextIfEmpty();
    this.balanceService.updateBalancesFromJournal(journalDTO);
  }

  private void initSecurityContextIfEmpty() {
    if (SecurityContextHolder.getContext().getAuthentication() == null) {
      SecurityContextHolder
          .getContext()
          .setAuthentication(new UsernamePasswordAuthenticationToken(
              "rabbit",
              null,
              List.of(
                  new SimpleGrantedAuthority(Privilege.read(Privilege.JOURNAL_ENTRY)),
                  new SimpleGrantedAuthority(Privilege.write(Privilege.JOURNAL_ENTRY)),
                  new SimpleGrantedAuthority(Privilege.execute(Privilege.JOURNAL_ENTRY))
              )
          ));
    }
  }

  @GetMapping(PATH_JOURNAL_ENTRIES)
  @PreAuthorize("hasAuthority('" + Privilege.READ + Privilege.JOURNAL_ENTRY + "')"
                    + " or #username.equals(authentication.name)")
  public ResponseEntity findBySpec(
      @RequestParam(required = false) String username,
      @RequestParam(required = false) Journal[] journals,
      @RequestParam(required = false) GameCategory gameCategory,
      @RequestParam(required = false) Ccy ccy,
      @RequestParam(required = false) Long refId,
      @RequestParam(required = false) String refType,
      @RequestParam(name = "createdDate_gte", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdDateGte,
      @RequestParam(name = "createdDate_lte", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdDateLte,
      @PageableDefault Pageable pageable
  ) {
    return ResponseEntity.ok(this.pagedAssembler.toResource(
        this.journalEntryRepository.findAll(
            JournalEntrySpecification.filterJournalEntry(
                StringUtils.trimToNull(username),
                journals,
                gameCategory,
                ccy,
                refId,
                refType,
                createdDateGte,
                createdDateLte
            ),
            pageable
        ),
        resourceAssemblerHelper.resourceAssembler()
    ));
  }

  @GetMapping(PATH_JOURNAL_ENTRIES + "/currentUser")
  public ResponseEntity getAllCurrentUserJournalEntries(
      @RequestParam(required = false) Journal[] journals,
      @RequestParam(required = false) GameCategory gameCategory,
      @RequestParam(required = false) Ccy ccy,
      @RequestParam(required = false) Long refId,
      @RequestParam(required = false) String refType,
      @RequestParam(name = "createdDate_gte", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdDateGte,
      @RequestParam(name = "createdDate_lte", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdDateLte,
      @PageableDefault Pageable pageable,
      Authentication authentication
  ) {
    if (authentication != null && StringUtils.isNotEmpty(authentication.getName())) {
      String username = StringUtils.trimToNull(authentication.getName());
      return ResponseEntity.ok(this.pagedAssembler.toResource(
          this.journalEntryRepository.findAll(
              JournalEntrySpecification.filterJournalEntry(
                  username,
                  journals,
                  gameCategory,
                  ccy,
                  refId,
                  refType,
                  createdDateGte,
                  createdDateLte
              ),
              pageable
          ),
          resourceAssemblerHelper.resourceAssembler()
      ));
    }

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  }
}
