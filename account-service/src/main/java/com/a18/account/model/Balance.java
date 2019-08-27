package com.a18.account.model;

import com.a18.common.constant.Ccy;
import com.a18.common.constant.GameCategory;
import com.a18.common.dto.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
@EqualsAndHashCode(of = {"account", "ccy", "gameCategory"})
@ToString(exclude = {"account"})
@Entity
@Table(name = "balance", schema = "account")
public class Balance extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @JsonIgnore
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private Account account;

  @Column(nullable = false, updatable = false)
  @Enumerated(EnumType.STRING)
  private Ccy ccy;

  @Enumerated(EnumType.STRING)
  @Column(precision = 2, updatable = false)
  private GameCategory gameCategory;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Integer withdrawLimit = 5;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Column(nullable = false, precision = 2)
  private BigDecimal balance = BigDecimal.ZERO;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Column(nullable = false, precision = 2)
  private BigDecimal bonusBalance = BigDecimal.ZERO;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Column(precision = 2, nullable = false)
  private BigDecimal turnoverAmt = BigDecimal.ZERO;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Column(precision = 2, insertable = false, nullable = false)
  private BigDecimal totalBettingAmt = BigDecimal.ZERO;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Column(precision = 2, insertable = false, nullable = false)
  private BigDecimal totalPayout = BigDecimal.ZERO;

  public String getUsername() {
    return account.getUsername();
  }
}
