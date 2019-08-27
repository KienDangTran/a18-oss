package com.a18.account.controller;

import com.a18.account.model.InProgressJournal;
import com.a18.account.model.repository.InProgressJournalRepository;
import com.a18.account.specification.InProgressJournalEntrySpecification;
import com.a18.common.constant.Ccy;
import com.a18.common.constant.GameCategory;
import com.a18.common.constant.Journal;
import com.a18.common.constant.Privilege;
import com.a18.common.util.ResourceAssemblerHelper;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RepositoryRestController
public class InProgressJournalController {
  private static final String PATH_IN_PROGRESS_JOURNAL_ENTRIES = "/inProgressJournals";

  @Autowired @Lazy private ResourceAssemblerHelper resourceAssemblerHelper;

  @Autowired @Lazy private PagedResourcesAssembler<InProgressJournal> pagedAssembler;

  @Autowired @Lazy private InProgressJournalRepository inProgressJournalRepository;

  @PreAuthorize("hasAuthority('" + Privilege.READ + Privilege.JOURNAL_ENTRY + "')"
                    + " or #username.equals(authentication.name)")
  @GetMapping(PATH_IN_PROGRESS_JOURNAL_ENTRIES)
  public ResponseEntity findBySpec(
      @RequestParam(required = false) String username,
      @RequestParam(required = false) Journal journal,
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
        this.inProgressJournalRepository.findAll(
            InProgressJournalEntrySpecification.filterInProgressJournalEntry(
                username,
                journal,
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
}
