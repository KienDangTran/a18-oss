package com.a18.common.constant;

public enum Journal {
  INVESTMENT, // receive money from investors
  DEPOSIT, // Deposit cash to cash account
  WITHDRAWAL, // Withdraw cash from cash account
  BET, // place bet with betting balance
  //ON_HOLD, // put cash on hold
  //RELEASE_HOLD, // release on hold cash
  BET_USING_BONUS, // place bet with bonus balance
  DECREASING_ADJUSTMENT, // Manual decrease balance adjustment
  INCREASING_ADJUSTMENT, // Manual increase balance adjustment
  PAYOUT, // payout winning bet
  COMMISSION, // commission for agencies
  BONUS, // Receive cash bonus
  CANCEL_BONUS, // Cancel cash bonus
}
