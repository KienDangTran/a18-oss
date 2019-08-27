package com.a18.account.model;

import com.a18.common.constant.Journal;
import com.a18.common.dto.BaseEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(of = {"balance", "journal", "refId", "refType"})
@ToString(exclude = {"balance"})
@Entity
@Table(name = "journal_entry", schema = "account")
public class JournalEntry extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Column(nullable = false, updatable = false)
  @Enumerated(EnumType.STRING)
  private Journal journal;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private Balance balance;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Column(precision = 2, nullable = false)
  private BigDecimal priorBalance = BigDecimal.ZERO;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Column(precision = 2, nullable = false)
  private BigDecimal priorBonusBalance = BigDecimal.ZERO;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Column(nullable = false, precision = 2)
  private BigDecimal drAmt = BigDecimal.ZERO;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Column(nullable = false, precision = 2)
  private BigDecimal crAmt = BigDecimal.ZERO;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Long refId;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private String refType;
}
