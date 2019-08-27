package com.a18.lottery.controller;

import com.a18.common.util.ResourceAssemblerHelper;
import com.a18.lottery.model.Issue;
import com.a18.lottery.model.repository.IssueRepository;
import com.a18.lottery.specification.IssueSpecification;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RepositoryRestController
public class IssueController {
  private static final String PATH_ISSUES = "/issues";

  @Autowired @Lazy private ResourceAssemblerHelper resourceAssemblerHelper;

  @Autowired @Lazy private PagedResourcesAssembler<Issue> pagedAssembler;

  @Autowired @Lazy private IssueRepository issueRepository;

  @GetMapping(PATH_ISSUES)
  public ResponseEntity findBySpec(
      @RequestParam(required = false) Long[] ids,
      @RequestParam(required = false) Integer[] schedulerIds,
      @RequestParam(required = false) Issue.IssueStatus[] statuses,
      @RequestParam(required = false) String code,
      @RequestParam(name = "openingTime_gte", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime openingTimeGte,
      @RequestParam(name = "openingTime_lte", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime openingTimeLte,
      @PageableDefault Pageable pageable
  ) {
    return ResponseEntity.ok(this.pagedAssembler.toResource(
        this.issueRepository.findAll(
            IssueSpecification.filterIssue(
                ids,
                schedulerIds,
                statuses,
                code,
                openingTimeGte,
                openingTimeLte
            ),
            pageable
        ),
        resourceAssemblerHelper.resourceAssembler()
    ));
  }

  @GetMapping(PATH_ISSUES + "/{id}")
  public ResponseEntity findById(@PathVariable("id") Long id) {
    return this.issueRepository
        .findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}
