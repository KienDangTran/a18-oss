package com.a18.account.controller;

import com.a18.account.model.BonusRecord;
import com.a18.account.model.repository.BonusRecordRepository;
import com.a18.account.specification.BonusRecordSpecification;
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
public class BonusRecordController {
  public static final String PATH_BONUS_RECORDS = "/bonusRecords";

  @Autowired @Lazy private BonusRecordRepository bonusRecordRepository;

  @Autowired @Lazy private ResourceAssemblerHelper resourceAssemblerHelper;

  @Autowired @Lazy private PagedResourcesAssembler<BonusRecord> pagedAssembler;

  @PreAuthorize("hasAuthority('" + Privilege.READ + Privilege.ACCOUNT + "')"
                    + " or #username.equals(authentication.name)")
  @GetMapping(PATH_BONUS_RECORDS)
  public ResponseEntity findBonusRecordBySpec(
      @RequestParam(required = false) String username,
      @RequestParam(required = false) Journal journal,
      @RequestParam(required = false) GameCategory gameCategory,
      @RequestParam(required = false) Ccy ccy,
      @RequestParam(required = false) String promotionCode,
      @RequestParam(name = "createdDate_gte", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdDateGte,
      @RequestParam(name = "createdDate_lte", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdDateLte,
      @PageableDefault Pageable pageable
  ) {
    return ResponseEntity.ok(pagedAssembler.toResource(
        this.bonusRecordRepository.findAll(
            BonusRecordSpecification.filterBonusRecord(
                username,
                journal,
                gameCategory,
                ccy,
                promotionCode,
                createdDateGte,
                createdDateLte
            ),
            pageable
        ),
        resourceAssemblerHelper.resourceAssembler()
    ));
  }
}
