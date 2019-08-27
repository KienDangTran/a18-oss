package com.a18.account.service;

import com.a18.account.model.Account;
import com.a18.account.model.AccountCategory;
import com.a18.account.model.Balance;
import com.a18.account.model.BonusRecord;
import com.a18.account.model.JournalEntry;
import com.a18.account.model.Promotion;
import com.a18.account.model.repository.BalanceRepository;
import com.a18.account.model.repository.BonusRecordRepository;
import com.a18.account.model.repository.PromotionRepository;
import com.a18.account.model.repository.TurnoverRepository;
import com.a18.common.constant.Ccy;
import com.a18.common.constant.GameCategory;
import com.a18.common.constant.Journal;
import com.a18.common.constant.NormalBalance;
import com.a18.common.dto.JournalDTO;
import com.a18.common.firebase.FCMUtils;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.a18.account.service.BalanceUpdater.adjustBalance;
import static com.a18.account.service.BalanceUpdater.adjustBonusBalance;
import static com.a18.account.service.BalanceUpdater.increaseTotalBettingAmt;
import static com.a18.account.service.BalanceUpdater.increaseTotalPayout;
import static com.a18.account.service.BalanceUpdater.increaseTurnoverAmt;
import static com.a18.common.constant.NormalBalance.CR;
import static com.a18.common.constant.NormalBalance.DR;

@Slf4j
@Service
public class BalanceService {
  @Autowired @Lazy private BalanceRepository balanceRepository;

  @Autowired @Lazy private JournalEntryService journalEntryService;

  @Autowired @Lazy private TurnoverRepository turnoverRepository;

  @Autowired @Lazy private PromotionRepository promotionRepository;

  @Autowired @Lazy private BonusRecordRepository bonusRecordRepository;

  @Autowired @Lazy private MessageSource messageSource;

  @Autowired @Lazy private FCMUtils fcmUtils;

  @Transactional
  public List<JournalEntry> updateBalancesFromJournal(JournalDTO journalDTO) {
    Optional<Balance> userAssetBal = this.balanceRepository
        .findByAccount_UsernameAndCcyAndGameCategoryAndAccount_CategoryInAndAccount_StatusIn(
            journalDTO.getUsername(),
            journalDTO.getCcy(),
            journalDTO.getGameCategory(),
            Set.of(AccountCategory.USER_ASSET),
            Set.of(Account.AccountStatus.ACTIVE)
        );

    if (!userAssetBal.isPresent()) return List.of();

    switch (journalDTO.getStatus()) {
      case IN_PROGRESS:
        this.journalEntryService.updateInProgressJournal(journalDTO, userAssetBal.get().getId());
        return List.of();
      case CANCELED:
        this.journalEntryService.deleteInProgressJournal(
            userAssetBal.get().getId(),
            journalDTO.getJournal(),
            journalDTO.getRefId(),
            journalDTO.getRefType()
        );
        return List.of();
      case FINAL:
        List<JournalEntry> journalEntries =
            this.updateUserAndCompanyAssert(journalDTO, userAssetBal.get());
        journalEntries = this.journalEntryService.saveAllJournalEntries(journalEntries);
        this.journalEntryService.deleteInProgressJournal(
            userAssetBal.get().getId(),
            journalDTO.getJournal(),
            journalDTO.getRefId(),
            journalDTO.getRefType()
        );
        this.balanceRepository.save(userAssetBal.get());
        this.notifyBalanceChanges(journalEntries, journalDTO.getRegistrationTokens());
        return journalEntries;
      default:
        throw new UnsupportedOperationException("journal status invalid: " + journalDTO);
    }
  }

  private List<JournalEntry> updateUserAndCompanyAssert(
      JournalDTO journalDTO,
      Balance userAssetBal
  ) {
    return Stream
        .concat(
            Stream.of(
                this.updateUserAsset(journalDTO, userAssetBal),
                this.updateCompanyAsset(journalDTO),
                this.updateCompanyEquity(journalDTO)
            ),
            this.autoApplyPromotion(journalDTO, userAssetBal)
                .stream()
                .map(bonusJournal -> Stream.of(
                    this.updateUserAsset(bonusJournal, userAssetBal),
                    this.updateCompanyAsset(bonusJournal),
                    this.updateCompanyEquity(bonusJournal)
                ))
                .flatMap(Stream::distinct)
        )
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toUnmodifiableList());
  }

  private Optional<JournalEntry> updateCompanyEquity(JournalDTO journalDTO) {
    switch (journalDTO.getJournal()) {
      case INVESTMENT:
        return this.updateCompanyBalance(journalDTO, AccountCategory.COMPANY_EQUITY, CR);
      default:
        return Optional.empty();
    }
  }

  private Optional<JournalEntry> updateCompanyAsset(JournalDTO journalDTO) {

    switch (journalDTO.getJournal()) {
      case INVESTMENT:
      case BET:
      case DECREASING_ADJUSTMENT:
      case CANCEL_BONUS:
      case BET_USING_BONUS:
        return this.updateCompanyBalance(journalDTO, AccountCategory.COMPANY_ASSET, DR);

      case INCREASING_ADJUSTMENT:
      case PAYOUT:
      case COMMISSION:
      case BONUS:
        return this.updateCompanyBalance(journalDTO, AccountCategory.COMPANY_ASSET, CR);

      default:
        return Optional.empty();
    }
  }

  private Optional<JournalEntry> updateCompanyBalance(
      JournalDTO journalDTO,
      AccountCategory accountCategory,
      NormalBalance normalBalance
  ) {
    return this.balanceRepository
        .findByAccountCategoryInAndAccount_StatusIn(
            Set.of(accountCategory),
            Set.of(Account.AccountStatus.ACTIVE)
        )
        .map(companyAssetBal -> {
          if (this.journalEntryService.isJournalDuplicated(journalDTO, companyAssetBal)) {
            log.warn("journal has already been processed: {}", journalDTO);
            return Optional.<JournalEntry>empty();
          }
          BigDecimal priorBal = companyAssetBal.getBalance();
          BigDecimal priorBonusBal = companyAssetBal.getBonusBalance();

          adjustBalance(companyAssetBal, journalDTO.getAmt(), normalBalance);
          this.balanceRepository.save(companyAssetBal);

          return this.journalEntryService.createJournalEntry(
              journalDTO,
              companyAssetBal,
              priorBal,
              priorBonusBal,
              normalBalance
          );
        })
        .orElseGet(Optional::empty);
  }

  private Optional<JournalEntry> updateUserAsset(JournalDTO journalDTO, Balance userAssetBal) {
    log.debug(
        "{} {} {} {} {} {}",
        String.format("%-10s", journalDTO.getGameCategory()),
        String.format("%-10s", journalDTO.getJournal()),
        String.format("%-10s", journalDTO.getUsername()),
        String.format("%-5s", journalDTO.getRefId()),
        String.format("%-8s", StringUtils.substringAfterLast(journalDTO.getRefType(), ".")),
        String.format("%3s %,.2f", journalDTO.getCcy(), journalDTO.getAmt())
    );

    if (this.journalEntryService.isJournalDuplicated(journalDTO, userAssetBal)) {
      log.warn("journal has already been processed: {}", journalDTO);
      return Optional.empty();
    }

    BigDecimal additionalTurnoverAmt =
        Objects.isNull(journalDTO.getAdditionalTurnoverAmt())
            || BigDecimal.ZERO.compareTo(journalDTO.getAdditionalTurnoverAmt()) == 0
        ? this.calcAdditionalTurnoverAmt(
            journalDTO.getJournal(),
            journalDTO.getCcy(),
            journalDTO.getGameCategory(),
            journalDTO.getAmt()
        )
        : journalDTO.getAdditionalTurnoverAmt();

    BigDecimal priorBal = userAssetBal.getBalance();
    BigDecimal priorBonusBal = userAssetBal.getBonusBalance();

    switch (journalDTO.getJournal()) {
      case DEPOSIT:
      case INCREASING_ADJUSTMENT:
        adjustBalance(userAssetBal, journalDTO.getAmt(), DR);
        increaseTurnoverAmt(userAssetBal, additionalTurnoverAmt);
        return this.journalEntryService.createJournalEntry(
            journalDTO,
            userAssetBal,
            priorBal,
            priorBonusBal,
            DR
        );

      case PAYOUT:
        increaseTotalPayout(userAssetBal, journalDTO);
        adjustBalance(userAssetBal, journalDTO.getAmt(), DR);
        return this.journalEntryService.createJournalEntry(
            journalDTO,
            userAssetBal,
            priorBal,
            priorBonusBal,
            DR
        );

      case COMMISSION:
        adjustBalance(userAssetBal, journalDTO.getAmt(), DR);
        return this.journalEntryService.createJournalEntry(
            journalDTO,
            userAssetBal,
            priorBal,
            priorBonusBal,
            DR
        );

      case BONUS:
        adjustBonusBalance(userAssetBal, journalDTO.getAmt(), DR);
        increaseTurnoverAmt(userAssetBal, additionalTurnoverAmt);
        return this.journalEntryService.createJournalEntry(
            journalDTO,
            userAssetBal,
            priorBal,
            priorBonusBal,
            DR
        );

      case WITHDRAWAL:
      case DECREASING_ADJUSTMENT:
        adjustBalance(userAssetBal, journalDTO.getAmt(), CR);
        return this.journalEntryService.createJournalEntry(
            journalDTO,
            userAssetBal,
            priorBal,
            priorBonusBal,
            CR
        );

      case BET:
        adjustBalance(userAssetBal, journalDTO.getAmt(), CR);
        increaseTotalBettingAmt(userAssetBal, journalDTO);
        return this.journalEntryService.createJournalEntry(
            journalDTO,
            userAssetBal,
            priorBal,
            priorBonusBal,
            CR
        );

      case CANCEL_BONUS:
      case BET_USING_BONUS:
        adjustBonusBalance(userAssetBal, journalDTO.getAmt(), CR);
        return this.journalEntryService.createJournalEntry(
            journalDTO,
            userAssetBal,
            priorBal,
            priorBonusBal,
            CR
        );
      default:
        return Optional.empty();
    }
  }

  private Set<JournalDTO> autoApplyPromotion(JournalDTO journalDTO, Balance userBalance) {
    return this.promotionRepository
        .findAllApplyingPromotions(
            journalDTO.getJournal(),
            journalDTO.getGameCategory(),
            journalDTO.getCcy(),
            true,
            LocalDateTime.now(),
            Set.of(Promotion.PromotionStatus.APPLYING)
        )
        .stream()
        .map(promotion -> createBonusJournal(journalDTO, userBalance, promotion))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toSet());
  }

  private Optional<JournalDTO> createBonusJournal(
      JournalDTO journalDTO,
      Balance userBalance,
      Promotion promotion
  ) {
    BonusRecord bonusRecord = this.bonusRecordRepository
        .findByBalanceAndPromotion(userBalance, promotion)
        .orElseGet(() -> {
          BonusRecord newBonusRecord = new BonusRecord();
          newBonusRecord.setBalance(userBalance);
          newBonusRecord.setPromotion(promotion);
          newBonusRecord.setPromotionId(promotion.getId());
          return newBonusRecord;
        });

    if (bonusRecord.getAppliedCount() >= promotion.getMaxApplyTime()) return Optional.empty();
    BigDecimal bonusAmt =
        Objects.equals(Promotion.AdjustType.AMT, promotion.getAdjustType())
        ? promotion.getBonusValue()
        : journalDTO.getAmt().multiply(promotion.getBonusValue()).movePointLeft(2);
    bonusRecord.setBonusAmt(bonusRecord.getBonusAmt().add(bonusAmt));
    bonusRecord.setAppliedCount((bonusRecord.getAppliedCount() + 1));
    this.bonusRecordRepository.save(bonusRecord);

    return Optional.of(
        JournalDTO.builder()
                  .amt(bonusAmt)
                  .additionalTurnoverAmt(bonusAmt.multiply(promotion.getTurnoverFactor()))
                  .ccy(promotion.getCcy())
                  .gameCategory(promotion.getGameCategory())
                  .journal(Journal.BONUS)
                  .username(journalDTO.getUsername())
                  .refId(journalDTO.getRefId())
                  .refType(journalDTO.getRefType())
                  .status(JournalDTO.JournalStatus.FINAL)
                  .build()
    );
  }

  private BigDecimal calcAdditionalTurnoverAmt(
      Journal journal,
      Ccy ccy,
      GameCategory gameCategory,
      BigDecimal adjustedAmt
  ) {
    return this.turnoverRepository
        .findByJournalAndGameCategoryAndCcy(journal, gameCategory, ccy)
        .map(turnover -> adjustedAmt.abs().multiply(turnover.getTurnoverFactor()))
        .orElse(BigDecimal.ZERO);
  }

  private void notifyBalanceChanges(List<JournalEntry> journalEntries, String registrationTokens) {
    if (journalEntries == null
        || journalEntries.isEmpty()
        || StringUtils.isBlank(registrationTokens)) {
      return;
    }

    journalEntries
        .stream()
        .filter(journalEntry ->
            AccountCategory.USER_ASSET.equals(journalEntry.getBalance().getAccount().getCategory()))
        .forEach(journalEntry -> {
          boolean isAdd = BigDecimal.ZERO.compareTo(Objects.requireNonNullElse(
              journalEntry.getDrAmt(),
              BigDecimal.ZERO
          )) < 0;
          String msgBody = this.messageSource.getMessage(
              "balance.changed.detail",
              new Object[] {
                  this.messageSource.getMessage(
                      journalEntry.getJournal().name(),
                      new Object[] {},
                      LocaleContextHolder.getLocale()
                  ),
                  isAdd ? "+" : "-",
                  String.format("%,.2f", isAdd ? journalEntry.getDrAmt() : journalEntry.getCrAmt()),
                  journalEntry.getBalance().getCcy().name(),
                  this.messageSource.getMessage(
                      journalEntry.getRefType(),
                      new Object[] {},
                      LocaleContextHolder.getLocale()
                  ),
                  journalEntry.getRefId()
              },
              LocaleContextHolder.getLocale()
          );

          this.fcmUtils.pushNotification(
              this.messageSource.getMessage(
                  "balance.changed",
                  new Object[] {},
                  LocaleContextHolder.getLocale()
              ),
              msgBody,
              JournalDTO.builder()
                        .refId(journalEntry.getRefId())
                        .refType(journalEntry.getRefType())
                        .journal(journalEntry.getJournal())
                        .amt(journalEntry.getCrAmt().max(journalEntry.getDrAmt()))
                        .build(),
              registrationTokens
          );
        });
  }
}
