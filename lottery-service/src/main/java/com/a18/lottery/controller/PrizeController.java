package com.a18.lottery.controller;

import com.a18.common.util.ResourceAssemblerHelper;
import com.a18.lottery.model.Issue;
import com.a18.lottery.model.Prize;
import com.a18.lottery.model.repository.PrizeRepository;
import com.a18.lottery.specification.PrizeSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RepositoryRestController
public class PrizeController {
  private static final String PATH_PRIZES = "/prizes";

  @Autowired @Lazy private ResourceAssemblerHelper resourceAssemblerHelper;

  @Autowired @Lazy private PagedResourcesAssembler<Prize> pagedAssembler;

  @Autowired @Lazy private PrizeRepository prizeRepository;

  @GetMapping(PATH_PRIZES)
  public ResponseEntity findBySpec(
      @RequestParam(required = false) Integer[] ids,
      @RequestParam(required = false) Integer[] schedulerIds,
      @RequestParam(required = false) Issue.IssueStatus status,
      @RequestParam(required = false) String code,
      @PageableDefault Pageable pageable
  ) {
    return ResponseEntity.ok(this.pagedAssembler.toResource(
        this.prizeRepository.findAll(
            PrizeSpecification.filterPrize(
                ids,
                schedulerIds,
                status,
                code
            ),
            pageable
        ),
        resourceAssemblerHelper.resourceAssembler()
    ));
  }
}
