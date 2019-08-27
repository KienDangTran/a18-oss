package com.a18.account.model;

import com.a18.common.constant.Journal;
import com.a18.common.dto.BaseEntity;
import com.a18.common.dto.JournalDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(of = {"balance", "journal", "refId", "refType"})
@ToString(exclude = {"balance"})
@Entity
@Table(name = "in_progress_journal", schema = "account")
public class InProgressJournal extends BaseEntity {

  public InProgressJournal(JournalDTO journalDTO, Long balanceId) {
    this.journal = journalDTO.getJournal();
    this.balanceId = balanceId;
    this.amt = journalDTO.getAmt();
    this.refId = journalDTO.getRefId();
    this.refType = journalDTO.getRefType();
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Column(nullable = false, updatable = false)
  @Enumerated(EnumType.STRING)
  private Journal journal;

  private Long balanceId;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Column(nullable = false, precision = 2)
  private BigDecimal amt = BigDecimal.ZERO;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Long refId;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private String refType;
}
