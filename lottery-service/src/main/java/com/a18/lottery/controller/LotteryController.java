package com.a18.lottery.controller;

import com.a18.common.util.ResourceAssemblerHelper;
import com.a18.lottery.model.Issue;
import com.a18.lottery.model.Lottery;
import com.a18.lottery.model.repository.LotteryRepository;
import com.a18.lottery.specification.LotterySpecification;
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
public class LotteryController {
  private static final String PATH_LOTTERIES = "/lotteries";

  @Autowired @Lazy private ResourceAssemblerHelper resourceAssemblerHelper;

  @Autowired @Lazy private PagedResourcesAssembler<Lottery> pagedAssembler;

  @Autowired @Lazy private LotteryRepository lotteryRepository;

  @GetMapping(PATH_LOTTERIES)
  public ResponseEntity findBySpec(
      @RequestParam(required = false) Integer[] ids,
      @RequestParam(required = false) Integer[] schedulerIds,
      @RequestParam(required = false) Issue.IssueStatus status,
      @RequestParam(required = false) String code,
      @PageableDefault Pageable pageable
  ) {
    return ResponseEntity.ok(this.pagedAssembler.toResource(
        this.lotteryRepository.findAll(
            LotterySpecification.filterIssue(
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
