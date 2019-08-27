package com.a18.lottery.validator;

import com.a18.common.constant.GameCategory;
import com.a18.common.dto.BalanceDTO;
import com.a18.common.util.AccountUtil;
import com.a18.lottery.model.BetItemGroup;
import com.a18.lottery.model.Issue;
import com.a18.lottery.model.Lottery;
import com.a18.lottery.model.Ticket;
import com.a18.lottery.model.repository.LotteryRepository;
import com.a18.lottery.model.repository.TicketRepository;
import com.a18.lottery.service.IssueService;
import com.a18.lottery.util.LotterySchemaUtil;
import com.a18.lottery.util.TicketUtil;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class TicketValidator implements Validator {

  @Autowired @Lazy private LotteryRepository lotteryRepository;

  @Autowired @Lazy private AccountUtil accountUtil;

  @Autowired @Lazy private IssueService issueService;

  @Override public boolean supports(Class<?> clazz) {
    return Ticket.class.equals(clazz);
  }

  @Transactional
  @Override public void validate(Object target, Errors errors) {
    Ticket ticket = (Ticket) target;

    if (this.isRequiredFieldsEmpty(errors)) return;

    this.validateLottery(ticket.getLotteryId(), errors).ifPresent(lottery -> {
          this.validateIssue(ticket, lottery, errors);
          this.validateBetContents(ticket, lottery, errors);
        }
    );
  }

  private boolean isRequiredFieldsEmpty(Errors errors) {
    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "lotteryId",
        "common.field.required",
        new Object[] {"lotteryId"}
    );
    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "ccy",
        "common.field.required",
        new Object[] {"ccy"}
    );
    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "betItemGroups",
        "common.field.required",
        new Object[] {"betItemGroups"}
    );

    return errors.hasErrors();
  }

  private Optional<Lottery> validateLottery(Integer lotteryId, Errors errors) {
    Optional<Lottery> lottery = this.lotteryRepository.findById(lotteryId);
    if (!lottery.isPresent()) {
      errors.rejectValue(
          "lotteryId",
          "lottery.not.found",
          new Object[] {lotteryId},
          "lottery.not.found"
      );
      return lottery;
    }

    if (Lottery.LotteryStatus.SUSPENDED.equals(lottery.get().getStatus())) {
      errors.rejectValue(
          "lotteryId",
          "lottery.is.suspended",
          new Object[] {lottery.get().getCode()},
          "lottery.is.suspended"
      );
    }

    return lottery;
  }

  private void validateIssue(Ticket ticket, Lottery lottery, Errors errors) {
    if (ticket.getIssueId() == null) return;

    this.issueService
        .findById(ticket.getIssueId())
        .ifPresentOrElse(
            issue -> {
              if (!Issue.IssueStatus.NEW.equals(issue.getStatus())
                  && !Issue.IssueStatus.OPENING.equals(issue.getStatus())) {
                errors.rejectValue(
                    "issueId",
                    "lottery.issue.has.been.closed.or.ended",
                    new Object[] {issue.getCode()},
                    "lottery.issue.has.been.closed.or.ended"
                );
              } else if (!lottery.getSchedulerId().equals(issue.getSchedulerId())) {
                errors.rejectValue(
                    "issueId",
                    "lottery.issue.does.not.belong.to.scheduler",
                    new Object[] {issue.getCode(), lottery.getSchedulerId()},
                    "lottery.issue.does.not.belong.to.scheduler"
                );
              }
            },
            () -> errors.rejectValue(
                "issueId",
                "lottery.issue.id.not.found",
                new Object[] {ticket.getIssueId()},
                "lottery.issue.id.not.found"
            )
        );
  }

  private void validateBetContents(Ticket ticket, Lottery lottery, Errors errors) {
    if (ticket.getBetItemGroups().isEmpty()) {
      errors.rejectValue("betItems", "ticket.no.bet.item.found");
      return;
    }

    if (ticket.getBetItemGroups().stream().anyMatch(group -> group.getBetUnit() == null)) {
      errors.reject("common.field.required", new Object[] {"betUnit"}, "common.field.required");
      return;
    }

    Map<String, Integer> betItems = TicketUtil.collectBetItems(
        ticket.getBetItemGroups()
              .stream()
              .collect(Collectors.toMap(
                  BetItemGroup::getBetUnit,
                  BetItemGroup::getBetItems,
                  (s, s2) -> StringUtils.joinWith(BetItemGroup.ITEM_CONTENTS_DELIMITER, s, s2)
              ))
    );
    if (betItems.isEmpty()) {
      errors.rejectValue("betItems", "ticket.no.bet.item.found");
      return;
    }

    Integer maxBetItem = LotterySchemaUtil.getLotteryMaxBetItem(lottery);

    if (betItems.size() > maxBetItem) {
      // validate maxBetItem
      errors.reject(
          "ticket.no.of.bet.items.must.be.less.than",
          new Object[] {betItems.size(), maxBetItem},
          "ticket.no.of.bet.items.must.be.less.than"
      );
    }

    betItems.forEach((betContent, betUnit) -> this.validateBetItem(
        lottery,
        betContent,
        betUnit,
        errors
    ));

    this.verifyUserBalance(ticket, lottery, betItems, errors);
  }

  private void validateBetItem(Lottery lottery, String betContent, Integer betUnit, Errors errors) {
    BigDecimal betItemMaxAmt = LotterySchemaUtil.getLotteryBetItemMaxAmt(lottery);
    BigDecimal unitPrice = LotterySchemaUtil.getLotteryBetUnitPrice(lottery);
    Integer betItemSize = LotterySchemaUtil.getLotteryBetItemSize(lottery);
    Integer betNoLength = LotterySchemaUtil.getLotteryBetNoLength(lottery);

    Set<String> betNumbers =
        Set.of(StringUtils.split(betContent, BetItemGroup.BET_NUMBERS_DELIMITER));

    if (betNumbers.size() != betItemSize) {
      // validate no. of bet number
      errors.reject(
          "ticket.bet.item.size.invalid",
          new Object[] {betContent},
          "ticket.bet.item.size.invalid"
      );
    } else if (betNumbers.stream().anyMatch(
        number -> StringUtils.trimToEmpty(number).length() != betNoLength)
    ) {
      // validate a bet number's length
      errors.reject(
          "ticket.bet.no.length.invalid",
          new Object[] {betNumbers},
          "ticket.bet.no.length.invalid"
      );
    } else if (betItemMaxAmt != null
        && unitPrice.multiply(BigDecimal.valueOf(betUnit)).compareTo(betItemMaxAmt) > 0) {
      // validate a single bet item's amt
      errors.reject(
          "ticket.bet.item.amt.cannot.excess",
          new Object[] {betContent, betItemMaxAmt},
          "ticket.bet.item.amt.cannot.excess"
      );
    }
  }

  private void verifyUserBalance(
      Ticket ticket,
      Lottery lottery,
      Map<String, Integer> betItems,
      Errors errors
  ) {
    String username = StringUtils.isBlank(ticket.getUsername())
                      ? SecurityContextHolder.getContext().getAuthentication().getName()
                      : ticket.getUsername();
    this.accountUtil.findBalanceByUsernameAndCcyAndGameCategory(
        username,
        ticket.getCcy(),
        GameCategory.LOTTERY
    ).ifPresentOrElse(
        bal -> validateUserBalanceAmt(bal, lottery, betItems, errors),
        () -> errors.reject(
            "account.username.not.found",
            new Object[] {username, ticket.getCcy(), GameCategory.LOTTERY},
            "account.username.not.found"
        )
    );
  }

  private void validateUserBalanceAmt(
      BalanceDTO bal,
      Lottery lottery,
      Map<String, Integer> betItems,
      Errors errors
  ) {
    BigDecimal unitPrice = LotterySchemaUtil.getLotteryBetUnitPrice(lottery);
    BigDecimal currentTicketTotalBetAmt =
        betItems.values()
                .stream()
                .map(integer -> unitPrice.multiply(BigDecimal.valueOf(integer)))
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);

    if (bal.getBalance().subtract(bal.getOnHoldAmt()).compareTo(currentTicketTotalBetAmt) < 0) {
      errors.reject(
          "ticket.total.betting.amt.is.exceed.balance",
          new Object[] {bal.getBalance(), bal.getOnHoldAmt()},
          "ticket.total.betting.amt.is.exceed.balance"
      );
    }
  }

  @Component("beforeCreateTicketValidator")
  @Lazy
  public static class BeforeCreateTicketValidator extends TicketValidator {
    @Autowired @Lazy private TicketRepository ticketRepository;

    @Override public void validate(Object target, Errors errors) {

      super.validate(target, errors);
      this.validateUserTicketExisted((Ticket) target, errors);
    }

    void validateUserTicketExisted(Ticket ticket, Errors errors) {
      boolean isUserHasTicketForCurrentIssue =
          this.ticketRepository.existsByIssueIdAndLotteryIdAndCcyAndUsername(
              ticket.getIssueId(),
              ticket.getLotteryId(),
              ticket.getCcy(),
              SecurityContextHolder.getContext().getAuthentication().getName()
          );

      if (isUserHasTicketForCurrentIssue) {
        errors.reject("ticket.user.existed");
      }
    }
  }

  @Component("beforeSaveTicketValidator")
  @Lazy
  public static class BeforeSaveTicketValidator extends TicketValidator {
  }
}
