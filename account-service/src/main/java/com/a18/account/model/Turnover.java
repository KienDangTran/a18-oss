package com.a18.account.model;

import com.a18.common.constant.Ccy;
import com.a18.common.constant.GameCategory;
import com.a18.common.constant.Journal;
import com.a18.common.dto.BaseEntity;
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

@Data
@EqualsAndHashCode(of = {"journal", "gameCategory", "ccy"})
@Entity
@Table(name = "turnover", schema = "account")
public class Turnover extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, updatable = false)
  @Enumerated(EnumType.STRING)
  private Journal journal;

  @Column(nullable = false, updatable = false)
  @Enumerated(EnumType.STRING)
  private GameCategory gameCategory;

  @Column(nullable = false, updatable = false)
  @Enumerated(EnumType.STRING)
  private Ccy ccy;

  @Column(nullable = false, precision = 2)
  private BigDecimal turnoverFactor;
}
