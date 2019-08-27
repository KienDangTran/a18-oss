package com.a18.account.service;

import com.a18.account.model.Balance;
import com.a18.common.constant.Journal;
import com.a18.common.constant.NormalBalance;
import com.a18.common.dto.JournalDTO;
import com.a18.common.exception.ApiException;
import java.math.BigDecimal;
import java.util.Objects;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
class BalanceUpdater {

  static void adjustBalance(Balance balance, BigDecimal amt, NormalBalance normalBalance) {
    if (Objects.equals(normalBalance, balance.getAccount().getCategory().getNormalBalance())) {
      balance.setBalance(balance.getBalance().add(amt.abs()));
      return;
    }

    if (balance.getBalance().compareTo(amt.abs()) < 0) {
      log.warn("journal.decreasing.amt.greater.than.balance");
    }

    balance.setBalance(balance.getBalance().subtract(amt.abs()));
  }

  static void adjustBonusBalance(
      Balance balance,
      BigDecimal amt,
      NormalBalance normalBalance
  ) {
    if (normalBalance.equals(balance.getAccount().getCategory().getNormalBalance())) {
      balance.setBonusBalance(balance.getBonusBalance().add(amt.abs()));
    } else if (balance.getBonusBalance().compareTo(amt.abs()) >= 0) {
      balance.setBonusBalance(balance.getBonusBalance().subtract(amt.abs()));
    } else {
      throw new ApiException("journal.decreasing.amt.greater.than.user.bonus.balance");
    }
  }

  static void increaseTurnoverAmt(Balance balance, BigDecimal additionalTurnoverAmt) {
    balance.setTurnoverAmt(balance.getTurnoverAmt().add(additionalTurnoverAmt.abs()));
  }

  static void increaseTotalBettingAmt(Balance balance, JournalDTO msg) {
    if (Journal.BET.equals(msg.getJournal())
        && msg.getGameCategory().equals(balance.getGameCategory())
        && msg.getCcy().equals(balance.getCcy())
        && msg.getUsername().equalsIgnoreCase(balance.getAccount().getUsername())) {
      balance.setTotalBettingAmt(balance.getTotalBettingAmt().add(msg.getAmt().abs()));
    }
  }

  static void increaseTotalPayout(Balance balance, JournalDTO msg) {
    if (Journal.PAYOUT.equals(msg.getJournal())
        && msg.getGameCategory().equals(balance.getGameCategory())
        && msg.getCcy().equals(balance.getCcy())
        && msg.getUsername().equalsIgnoreCase(balance.getAccount().getUsername())) {
      balance.setTotalPayout(balance.getTotalPayout().add(msg.getAmt().abs()));
    }
  }

  //static void adjustOnHoldAmt(Balance userBalance, BigDecimal amt, boolean isIncrement) {
  //  userBalance.setOnHoldAmt(
  //      isIncrement
  //      ? userBalance.getOnHoldAmt().add(amt.abs())
  //      : userBalance.getOnHoldAmt().subtract(amt.abs())
  //  );
  //}
}
