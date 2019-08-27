package com.a18.lottery.model;

import com.a18.common.dto.BaseEntity;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = {"code"})
@Entity
@Table(name = "lottery_schema", schema = "lottery")
public class LotterySchema extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, unique = true, updatable = false)
  private String code;

  @Column(nullable = false)
  private Integer betItemSize;

  @Column(nullable = false)
  private Integer betNoLength;

  @Column(nullable = false, precision = 2)
  private BigDecimal defaultBetUnitPrice;

  @Column(nullable = false, precision = 2)
  private BigDecimal defaultWinUnitPrice;

  private Integer defaultMaxBetItem;

  @Column(precision = 2)
  private BigDecimal defaultBetItemMaxAmt;

  @Column(precision = 2)
  private BigDecimal defaultMaxPayout;
}

