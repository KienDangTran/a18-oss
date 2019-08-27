package com.a18.payment.controller;

import com.a18.common.constant.Ccy;
import com.a18.common.constant.GameCategory;
import com.a18.common.constant.Journal;
import com.a18.common.constant.Privilege;
import com.a18.common.exception.ApiException;
import com.a18.common.util.JournalUtil;
import com.a18.common.util.ResourceAssemblerHelper;
import com.a18.payment.model.Tx;
import com.a18.payment.model.dto.TxDTO;
import com.a18.payment.service.TxService;
import com.a18.payment.specification.TxSpecification;
import com.a18.payment.validator.BeforeCreateTxValidator;
import java.time.LocalDateTime;
import java.util.Objects;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.core.RepositoryConstraintViolationException;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TxController {

  private static final String PATH_TRANSACTIONS = "/txes";

  @Autowired @Lazy private BeforeCreateTxValidator beforeCreateTxValidator;

  @Autowired @Lazy private TxService txService;

  @Autowired @Lazy private PagedResourcesAssembler<Tx> pagedAssembler;

  @Autowired @Lazy private ResourceAssemblerHelper resourceAssemblerHelper;

  @InitBinder("txDTO")
  public void setupBinder(WebDataBinder binder) {
    binder.addValidators(beforeCreateTxValidator);
  }

  @PostMapping(PATH_TRANSACTIONS)
  public ResponseEntity createTxRequest(
      @Valid @RequestBody TxDTO txDTO,
      BindingResult bindingResult,
      Authentication auth
  ) {
    if (bindingResult.hasErrors()) throw new RepositoryConstraintViolationException(bindingResult);

    return ResponseEntity.ok().body(
        this.txService
            .createTxRequest(
                txDTO.withCode(JournalUtil.genJournalCode(txDTO.getJournal(), auth.getName()))
                     .withUsername(auth.getName())
                     .withStatus(Tx.TxStatus.NEW)
            )
    );
  }

  @PatchMapping(PATH_TRANSACTIONS + "/{id}")
  @PreAuthorize("hasAuthority('" + Privilege.EXEC + Privilege.TX + "') ")
  public ResponseEntity approveTx(@PathVariable Long id, @RequestBody TxDTO txDTO) {
    if (Objects.equals(txDTO.getStatus(), Tx.TxStatus.REJECTED)
        && StringUtils.isBlank(txDTO.getRemark())) {
      throw new ApiException("common.field.required", "remark");
    }

    return ResponseEntity.ok(this.txService.approveTx(txDTO.withId(id)));
  }

  @GetMapping(PATH_TRANSACTIONS + "/{id}")
  public ResponseEntity getTxDetails(@PathVariable Long id) {
    return ResponseEntity.ok().body(this.txService.getAndProcessTxResult(id));
  }

  @DeleteMapping(PATH_TRANSACTIONS + "/{id}")
  public ResponseEntity deleteTx(@PathVariable Long id) {
    this.txService.deleteTx(id);
    return ResponseEntity.noContent().build();
  }

  @PreAuthorize("hasAuthority('" + Privilege.READ + Privilege.TX + "')"
                    + " or #username.equals(authentication.name)")
  @GetMapping(PATH_TRANSACTIONS)
  public ResponseEntity findBySpec(
      @RequestParam(required = false) Long[] ids,
      @RequestParam(required = false) String username,
      @RequestParam(required = false) Journal journal,
      @RequestParam(required = false) GameCategory gameCategory,
      @RequestParam(required = false) Ccy ccy,
      @RequestParam(required = false) Integer bankId,
      @RequestParam(required = false) Integer paymentMethodId,
      @RequestParam(required = false) String remark,
      @RequestParam(required = false) String invoiceNo,
      @RequestParam(name = " createdDate_gte", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdDateGte,
      @RequestParam(name = " createdDate_lte", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdDateLte,
      @PageableDefault Pageable pageable
  ) {
    return ResponseEntity.ok(this.pagedAssembler.toResource(
        this.txService.findBySpec(
            TxSpecification.filterTx(
                ids,
                username,
                journal,
                gameCategory,
                ccy,
                bankId,
                paymentMethodId,
                remark,
                invoiceNo,
                createdDateGte,
                createdDateLte
            ),
            pageable
        ),
        resourceAssemblerHelper.resourceAssembler()
    ));
  }
}
