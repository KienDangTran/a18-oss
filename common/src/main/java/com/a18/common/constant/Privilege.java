package com.a18.common.constant;

public interface Privilege {
  String READ = "READ_", WRITE = "WRITE_", EXEC = "EXEC_";

  // auth
  String
      STAFF = "STAFF",
      USER = "USER",
      ROLE = "ROLE",
      AUTHORITY = "AUTHORITY";

  // account
  String
      ACCOUNT = "ACCOUNT",
      AGENT_LEVEL = "AGENT_LEVEL",
      JOURNAL_ENTRY = "JOURNAL_ENTRY",
      TURNOVER = "TURNOVER";

  // lottery
  String
      SCHEDULER = "SCHEDULER",
      ISSUE = "ISSUE",
      RULE = "RULE",
      PRIZE = "PRIZE",
      LOTTERY = "LOTTERY",
      TICKET = "TICKET",
      DRAW_RESULT = "DRAW_RESULT";

  // payment
  String
      BANK = "BANK",
      PAYMENT_CARD = "PAYMENT_CARD",
      PAYMENT_METHOD = "PAYMENT_METHOD",
      PAYMENT_CHANNEL = "PAYMENT_CHANNEL",
      PAYMENT_VENDOR = "PAYMENT_VENDOR",
      PROMOTION = "PROMOTION",
      TX = "TX";

  static String read(String authority) {
    return READ + authority;
  }

  static String write(String authority) {
    return WRITE + authority;
  }

  static String execute(String authority) {
    return EXEC + authority;
  }
}
