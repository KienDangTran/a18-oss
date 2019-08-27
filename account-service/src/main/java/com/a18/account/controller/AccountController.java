package com.a18.account.controller;

import com.a18.account.model.Account;
import com.a18.account.model.AccountCategory;
import com.a18.account.model.repository.AccountRepository;
import com.a18.account.specification.AccountSpecification;
import com.a18.common.constant.Privilege;
import com.a18.common.util.ResourceAssemblerHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
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
public class AccountController {
  public static final String PATH_ACCOUNTS = "/accounts";

  @Autowired @Lazy private ResourceAssemblerHelper resourceAssemblerHelper;

  @Autowired @Lazy private PagedResourcesAssembler<Account> pagedAssembler;

  @Autowired @Lazy private AccountRepository accountRepository;

  @PreAuthorize("hasAuthority('" + Privilege.READ + Privilege.ACCOUNT + "')"
                    + " or #username.equals(authentication.name)")
  @GetMapping(PATH_ACCOUNTS)
  public ResponseEntity findBySpec(
      @RequestParam(required = false) String username,
      @RequestParam(required = false) AccountCategory category,
      @RequestParam(required = false) Account.AccountStatus status,
      @PageableDefault Pageable pageable
  ) {
    return ResponseEntity.ok(this.pagedAssembler.toResource(
        this.accountRepository.findAll(
            AccountSpecification.filterAccount(StringUtils.trimToNull(username), category, status),
            pageable
        ),
        resourceAssemblerHelper.resourceAssembler()
    ));
  }

  @GetMapping(PATH_ACCOUNTS + "/currentUser")
  public ResponseEntity getCurrentUser(
      Authentication authentication,
      @RequestParam(required = false) AccountCategory category,
      @RequestParam(required = false) Account.AccountStatus status,
      @PageableDefault Pageable pageable
  ) {
    if (authentication != null && StringUtils.isNotEmpty(authentication.getName())) {
      Page<Account> page = this.accountRepository.findAll(
          AccountSpecification.filterAccount(
              StringUtils.trimToNull(authentication.getName()),
              category,
              status
          ),
          pageable
      );

      return ResponseEntity.ok(this.pagedAssembler.toResource(
          page,
          this.resourceAssemblerHelper.resourceAssembler()
      ));
    }

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  }
}
