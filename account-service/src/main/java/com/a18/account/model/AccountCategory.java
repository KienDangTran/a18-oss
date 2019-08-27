package com.a18.account.model;

import com.a18.common.constant.NormalBalance;

import static com.a18.common.constant.NormalBalance.CR;
import static com.a18.common.constant.NormalBalance.DR;

public enum AccountCategory {
  COMPANY_ASSET(DR),
  COMPANY_EQUITY(CR),
  COMPANY_LIABILITY(CR),
  COMPANY_REVENUE(CR),
  COMPANY_EXPENSE(CR),
  USER_ASSET(DR);

  private NormalBalance normalBalance;

  AccountCategory(NormalBalance normalBalance) {
    this.normalBalance = normalBalance;
  }

  public NormalBalance getNormalBalance() {
    return normalBalance;
  }
}