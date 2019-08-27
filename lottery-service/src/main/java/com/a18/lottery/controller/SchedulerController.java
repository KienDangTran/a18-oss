package com.a18.lottery.controller;

import com.a18.common.util.ResourceAssemblerHelper;
import com.a18.lottery.model.LotteryCategory;
import com.a18.lottery.model.Scheduler;
import com.a18.lottery.model.repository.SchedulerRepository;
import com.a18.lottery.specification.SchedulerSpecification;
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
public class SchedulerController {
  private static final String PATH_SCHEDULERS = "/schedulers";

  @Autowired @Lazy private SchedulerRepository schedulerRepository;

  @Autowired @Lazy private PagedResourcesAssembler<Scheduler> pagedAssembler;

  @Autowired @Lazy private ResourceAssemblerHelper resourceAssemblerHelper;

  @GetMapping(PATH_SCHEDULERS)
  public ResponseEntity findBySpec(
      @RequestParam(required = false) Integer[] ids,
      @RequestParam(required = false) LotteryCategory category,
      @RequestParam(required = false) String code,
      @RequestParam(required = false) String openDay,
      @RequestParam(required = false) Scheduler.SchedulerStatus status,
      @PageableDefault Pageable pageable
  ) {
    return ResponseEntity.ok(this.pagedAssembler.toResource(
        this.schedulerRepository.findAll(
            SchedulerSpecification.filterScheduler(
                ids,
                category,
                code,
                openDay,
                status
            ),
            pageable
        ),
        resourceAssemblerHelper.resourceAssembler()
    ));
  }
}
