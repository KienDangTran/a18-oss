package com.a18.payment.controller;

import com.a18.common.constant.Ccy;
import com.a18.common.constant.Privilege;
import com.a18.common.util.ResourceAssemblerHelper;
import com.a18.payment.model.PaymentCard;
import com.a18.payment.model.PaymentCardType;
import com.a18.payment.model.repository.PaymentCardRepository;
import com.a18.payment.specification.PaymentCardSpecification;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RepositoryRestController
public class PaymentCardController {
  private static final String PATH_PAYMENT_CARDS = "paymentCards";

  @Autowired @Lazy private PagedResourcesAssembler<PaymentCard> pagedAssembler;

  @Autowired @Lazy private ResourceAssemblerHelper resourceAssemblerHelper;

  @Autowired @Lazy private PaymentCardRepository paymentCardRepository;

  @PreAuthorize("hasAuthority('" + Privilege.READ + Privilege.PAYMENT_CARD + "')"
                    + " or #username.equals(authentication.name)")
  @GetMapping(PATH_PAYMENT_CARDS)
  public ResponseEntity findBySpec(
      @RequestParam(required = false) String username,
      @RequestParam(required = false) Ccy ccy,
      @RequestParam(required = false) PaymentCardType type,
      @RequestParam(required = false) Integer bankId,
      @RequestParam(required = false) String bankAccount,
      @RequestParam(required = false) String cardNo,
      @RequestParam(required = false) String ownerName,
      @PageableDefault Pageable pageable
  ) {
    return ResponseEntity.ok(this.pagedAssembler.toResource(
        this.paymentCardRepository.findAll(
            PaymentCardSpecification.filterPaymentCard(
                StringUtils.trimToNull(username),
                ccy,
                type,
                bankId,
                StringUtils.trimToNull(bankAccount),
                StringUtils.trimToNull(cardNo),
                StringUtils.trimToNull(ownerName)
            ),
            pageable
        ),
        resourceAssemblerHelper.resourceAssembler()
    ));
  }

  @GetMapping(PATH_PAYMENT_CARDS + "/currentUser")
  public ResponseEntity getAllCurrentUserPaymentCards(
      @RequestParam(required = false) Ccy ccy,
      @RequestParam(required = false) PaymentCardType type,
      @RequestParam(required = false) Integer bankId,
      @RequestParam(required = false) String bankAccount,
      @RequestParam(required = false) String cardNo,
      @RequestParam(required = false) String ownerName,
      Authentication authentication,
      @PageableDefault Pageable pageable
  ) {
    if (authentication == null || StringUtils.isEmpty(authentication.getName())) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    return ResponseEntity.ok(this.pagedAssembler.toResource(
        this.paymentCardRepository.findAll(
            PaymentCardSpecification.filterPaymentCard(
                StringUtils.trimToNull(authentication.getName()),
                ccy,
                type,
                bankId,
                StringUtils.trimToNull(bankAccount),
                StringUtils.trimToNull(cardNo),
                StringUtils.trimToNull(ownerName)
            ),
            pageable
        ),
        resourceAssemblerHelper.resourceAssembler()
    ));
  }
}
