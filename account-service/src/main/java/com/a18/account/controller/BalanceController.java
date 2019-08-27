package com.a18.account.controller;

import com.a18.account.model.Account;
import com.a18.account.model.AccountCategory;
import com.a18.account.model.repository.BalanceRepository;
import com.a18.account.model.repository.InProgressJournalRepository;
import com.a18.common.constant.Ccy;
import com.a18.common.constant.GameCategory;
import com.a18.common.dto.BalanceDTO;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import static com.a18.common.util.AccountUtil.PATH_SEARCH_BALANCE;

@RestController
public class BalanceController {

  @Autowired @Lazy private BalanceRepository balanceRepository;

  @Autowired @Lazy private InProgressJournalRepository inProgressJournalRepository;

  @PostMapping(value = PATH_SEARCH_BALANCE)
  public ResponseEntity retrieveUserBalance(WebRequest request) {
    return this.balanceRepository
        .findByAccount_UsernameAndCcyAndGameCategoryAndAccount_CategoryInAndAccount_StatusIn(
            StringUtils.trimToNull(request.getParameter("username")),
            EnumUtils.getEnum(Ccy.class, request.getParameter("ccy")),
            EnumUtils.getEnum(GameCategory.class, request.getParameter("gameCategory")),
            Set.of(AccountCategory.USER_ASSET),
            Set.of(Account.AccountStatus.ACTIVE)
        )
        .<ResponseEntity>map(balance -> ResponseEntity.ok(
            BalanceDTO.builder()
                      .ccy(balance.getCcy())
                      .gameCategory(balance.getGameCategory())
                      .balance(balance.getBalance())
                      .onHoldAmt(Objects.requireNonNullElse(
                          this.inProgressJournalRepository.getTotalOnHoldAmt(balance.getId()),
                          BigDecimal.ZERO
                      ))
                      .bonusBalance(Objects.requireNonNullElse(
                          balance.getBonusBalance(),
                          BigDecimal.ZERO
                      ))
                      .turnoverAmt(Objects.requireNonNullElse(
                          balance.getTurnoverAmt(),
                          BigDecimal.ZERO
                      ))
                      .withdrawLimit(Objects.requireNonNullElse(
                          balance.getWithdrawLimit(),
                          0
                      ))
                      .totalBettingAmt(Objects.requireNonNullElse(
                          balance.getTotalBettingAmt(),
                          BigDecimal.ZERO
                      ))
                      .build()
        ))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                                       .body("account.username.not.found"));
  }
}
