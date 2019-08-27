package com.a18.account.service;

import com.a18.account.model.Balance;
import com.a18.account.model.InProgressJournal;
import com.a18.account.model.JournalEntry;
import com.a18.account.model.repository.InProgressJournalRepository;
import com.a18.account.model.repository.JournalEntryRepository;
import com.a18.common.constant.Journal;
import com.a18.common.constant.NormalBalance;
import com.a18.common.dto.JournalDTO;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import static com.a18.common.constant.NormalBalance.CR;
import static com.a18.common.constant.NormalBalance.DR;

@Service
public class JournalEntryService {

  @Autowired @Lazy private JournalEntryRepository journalEntryRepository;

  @Autowired @Lazy private InProgressJournalRepository inProgressJournalRepository;

  void updateInProgressJournal(JournalDTO journalDTO, Long balanceId) {
    if (!Objects.equals(journalDTO.getStatus(), JournalDTO.JournalStatus.IN_PROGRESS)) return;
    this.inProgressJournalRepository.getByBalanceIdAndJournalAndRefIdAndRefType(
        balanceId,
        journalDTO.getJournal(),
        journalDTO.getRefId(),
        journalDTO.getRefType()
    ).ifPresentOrElse(
        inProgressJournal -> {
          inProgressJournal.setAmt(journalDTO.getAmt());
          this.inProgressJournalRepository.save(inProgressJournal);
        },
        () -> this.inProgressJournalRepository.save(new InProgressJournal(journalDTO, balanceId))
    );
  }

  boolean isJournalDuplicated(JournalDTO journalDTO, Balance balance) {
    return this.journalEntryRepository.existsByBalanceAndJournalAndRefIdAndRefType(
        balance,
        journalDTO.getJournal(),
        journalDTO.getRefId(),
        journalDTO.getRefType()
    );
  }

  Optional<JournalEntry> createJournalEntry(
      JournalDTO journalDTO,
      Balance balance,
      BigDecimal priorBal,
      BigDecimal priorBonusBal,
      NormalBalance normalBalance
  ) {
    if (journalDTO.getAmt().compareTo(BigDecimal.ZERO) <= 0) return Optional.empty();

    JournalEntry entry = new JournalEntry();
    entry.setBalance(balance);
    entry.setJournal(journalDTO.getJournal());
    entry.setRefId(journalDTO.getRefId());
    entry.setRefType(journalDTO.getRefType());
    entry.setPriorBalance(priorBal);
    entry.setPriorBonusBalance(priorBonusBal);
    entry.setDrAmt(DR.equals(normalBalance) ? journalDTO.getAmt() : BigDecimal.ZERO);
    entry.setCrAmt(CR.equals(normalBalance) ? journalDTO.getAmt() : BigDecimal.ZERO);

    return Optional.of(entry);
  }

  List<JournalEntry> saveAllJournalEntries(List<JournalEntry> journalEntries) {
    return this.journalEntryRepository.saveAll(journalEntries);
  }

  void deleteInProgressJournal(Long balanceId, Journal journal, Long refId, String refType) {
    this.inProgressJournalRepository.deleteAllByBalanceIdAndJournalAndRefIdAndRefType(
        balanceId,
        journal,
        refId,
        refType
    );
  }
}
