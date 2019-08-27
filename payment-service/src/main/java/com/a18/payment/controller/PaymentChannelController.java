package com.a18.payment.controller;

import com.a18.common.constant.Ccy;
import com.a18.common.util.ResourceAssemblerHelper;
import com.a18.payment.model.PaymentCardType;
import com.a18.payment.model.PaymentChannel;
import com.a18.payment.model.repository.PaymentChannelRepository;
import com.a18.payment.specification.PaymentChannelSpecification;
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
public class PaymentChannelController {
  private static final String PATH_PAYMENT_CHANNELS = "/paymentChannels";

  @Autowired @Lazy private PagedResourcesAssembler<PaymentChannel> pagedAssembler;

  @Autowired @Lazy private ResourceAssemblerHelper resourceAssemblerHelper;

  @Autowired @Lazy private PaymentChannelRepository paymentChannelRepository;

  @GetMapping(PATH_PAYMENT_CHANNELS)
  public ResponseEntity findBySpec(
      @RequestParam(required = false) Integer[] ids,
      @RequestParam(required = false) Integer paymentVendorId,
      @RequestParam(required = false) Integer bankId,
      @RequestParam(required = false) Integer paymentMethodId,
      @RequestParam(required = false) Ccy ccy,
      @RequestParam(required = false) Boolean bankAccountRequired,
      @RequestParam(required = false) PaymentCardType requiredCardType,
      @RequestParam(required = false) Boolean deposit,
      @RequestParam(required = false) Boolean withdrawal,
      @RequestParam(required = false) Boolean autoApprove,
      @PageableDefault Pageable pageable
  ) {
    return ResponseEntity.ok(this.pagedAssembler.toResource(
        this.paymentChannelRepository.findAll(
            PaymentChannelSpecification.filterPaymentChannel(
                ids,
                paymentVendorId,
                bankId,
                paymentMethodId,
                ccy,
                bankAccountRequired,
                requiredCardType,
                deposit,
                withdrawal,
                autoApprove
            ),
            pageable
        ),
        resourceAssemblerHelper.resourceAssembler()
    ));
  }
}
