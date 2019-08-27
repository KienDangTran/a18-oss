package com.a18.lottery.controller;

import com.a18.common.util.ResourceAssemblerHelper;
import com.a18.lottery.model.DrawResult;
import com.a18.lottery.model.repository.DrawResultRepository;
import com.a18.lottery.specification.DrawResultSpecification;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RepositoryRestController
public class DrawResultController {
  private static final String PATH_DRAW_RESULTS = "/drawResults";

  @Autowired @Lazy private DrawResultRepository drawResultRepository;

  @Autowired @Lazy private PagedResourcesAssembler<DrawResult> pagedAssembler;

  @Autowired @Lazy private ResourceAssemblerHelper resourceAssemblerHelper;

  @Transactional
  @GetMapping(PATH_DRAW_RESULTS)
  public ResponseEntity findBySpec(
      @RequestParam(required = false) Long[] ids,
      @RequestParam(required = false) Long[] issueIds,
      @RequestParam(required = false) Integer[] prizeIds,
      @RequestParam(name = " createdDate_gte", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdDateGte,
      @RequestParam(name = " createdDate_lte", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdDateLte,
      @PageableDefault Pageable pageable
  ) {
    return ResponseEntity.ok(this.pagedAssembler.toResource(
        this.drawResultRepository.findAll(
            DrawResultSpecification.filterDrawResult(
                ids,
                issueIds,
                prizeIds,
                createdDateGte,
                createdDateLte
            ),
            pageable
        ),
        resourceAssemblerHelper.resourceAssembler()
    ));
  }
}
