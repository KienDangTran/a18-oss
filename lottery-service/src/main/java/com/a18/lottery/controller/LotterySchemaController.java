package com.a18.lottery.controller;

import com.a18.common.util.ResourceAssemblerHelper;
import com.a18.lottery.model.LotterySchema;
import com.a18.lottery.model.repository.LotterySchemaRepository;
import com.a18.lottery.specification.LotterySchemaSpecification;
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
public class LotterySchemaController {
  private static final String PATH_LOTTERY_SCHEMAS = "/lotterySchemas";

  @Autowired @Lazy private ResourceAssemblerHelper resourceAssemblerHelper;

  @Autowired @Lazy private PagedResourcesAssembler<LotterySchema> pagedAssembler;

  @Autowired @Lazy private LotterySchemaRepository lotterySchemaRepository;

  @GetMapping(PATH_LOTTERY_SCHEMAS)
  public ResponseEntity findBySpec(
      @RequestParam(required = false) Long[] ids,
      @RequestParam(required = false) String code,
      @PageableDefault Pageable pageable
  ) {
    return ResponseEntity.ok(this.pagedAssembler.toResource(
        this.lotterySchemaRepository.findAll(
            LotterySchemaSpecification.filterLotterySchema(ids, code),
            pageable
        ),
        resourceAssemblerHelper.resourceAssembler()
    ));
  }
}
