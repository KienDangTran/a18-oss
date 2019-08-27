package com.a18.account.controller;

import com.a18.account.model.Promotion;
import com.a18.account.model.repository.PromotionRepository;
import com.a18.account.specification.PromotionSpecification;
import com.a18.common.constant.Ccy;
import com.a18.common.constant.GameCategory;
import com.a18.common.constant.Journal;
import com.a18.common.util.ResourceAssemblerHelper;
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
public class PromotionController {
  private static final String PATH_PROMOTIONS = "/promotions";

  @Autowired @Lazy private PagedResourcesAssembler<Promotion> pagedAssembler;

  @Autowired @Lazy private ResourceAssemblerHelper resourceAssemblerHelper;

  @Autowired @Lazy private PromotionRepository promotionRepository;

  @GetMapping(PATH_PROMOTIONS)
  public ResponseEntity findBySpec(
      @RequestParam(required = false) Integer[] ids,
      @RequestParam(required = false) String code,
      @RequestParam(required = false) Boolean autoApply,
      @RequestParam(required = false) Journal journal,
      @RequestParam(required = false) Ccy ccy,
      @RequestParam(required = false) GameCategory gameCategory,
      @RequestParam(required = false) String description,
      @RequestParam(required = false) Promotion.PromotionStatus status,
      @PageableDefault Pageable pageable
  ) {
    return ResponseEntity.ok(this.pagedAssembler.toResource(
        this.promotionRepository.findAll(
            PromotionSpecification.filterPromotion(
                ids,
                code,
                autoApply,
                journal,
                ccy,
                gameCategory,
                description,
                status
            ),
            pageable
        ),
        resourceAssemblerHelper.resourceAssembler()
    ));
  }
}
