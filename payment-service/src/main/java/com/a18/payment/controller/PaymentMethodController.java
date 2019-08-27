package com.a18.payment.controller;

import com.a18.common.util.ResourceAssemblerHelper;
import com.a18.payment.model.PaymentMethod;
import com.a18.payment.model.repository.PaymentMethodRepository;
import com.a18.payment.specification.PaymentMethodSpecification;
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
public class PaymentMethodController {
  private static final String PATH_BANKS = "/paymentMethods";

  @Autowired @Lazy private PagedResourcesAssembler<PaymentMethod> pagedAssembler;

  @Autowired @Lazy private ResourceAssemblerHelper resourceAssemblerHelper;

  @Autowired @Lazy private PaymentMethodRepository paymentMethodRepository;

  @GetMapping(PATH_BANKS)
  public ResponseEntity findBySpec(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) PaymentMethod.PaymentMethodStatus status,
      @PageableDefault Pageable pageable
  ) {
    return ResponseEntity.ok(this.pagedAssembler.toResource(
        this.paymentMethodRepository.findAll(
            PaymentMethodSpecification.filterPaymentMethod(
                name,
                status
            ),
            pageable
        ),
        resourceAssemblerHelper.resourceAssembler()
    ));
  }
}
