package com.a18.payment.controller;

import com.a18.common.util.ResourceAssemblerHelper;
import com.a18.payment.model.Bank;
import com.a18.payment.model.repository.BankRepository;
import com.a18.payment.specification.BankSpecification;
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
public class BankController {
  private static final String PATH_BANKS = "/banks";

  @Autowired @Lazy private PagedResourcesAssembler<Bank> pagedAssembler;

  @Autowired @Lazy private ResourceAssemblerHelper resourceAssemblerHelper;

  @Autowired @Lazy private BankRepository bankRepository;

  @GetMapping(PATH_BANKS)
  public ResponseEntity findBySpec(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) Bank.BankStatus status,
      @PageableDefault Pageable pageable
  ) {
    return ResponseEntity.ok(this.pagedAssembler.toResource(
        this.bankRepository.findAll(
            BankSpecification.filterBank(
                name,
                status
            ),
            pageable
        ),
        resourceAssemblerHelper.resourceAssembler()
    ));
  }
}
