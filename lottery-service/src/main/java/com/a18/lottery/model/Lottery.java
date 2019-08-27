package com.a18.lottery.model;

import com.a18.common.dto.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(of = {"code"})
@ToString(exclude = {"scheduler", "lotterySchema"})
@Data
@Entity
@Table(name = "lottery", schema = "lottery")
public class Lottery extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, unique = true, updatable = false)
  private String code;

  @Column(name = "lottery_schema_id", insertable = false, updatable = false, nullable = false)
  private Integer lotterySchemaId;

  @JsonIgnore
  @ManyToOne(optional = false)
  private LotterySchema lotterySchema;

  @Column(name = "scheduler_id", insertable = false, updatable = false, nullable = false)
  private Integer schedulerId;

  @Column(nullable = false, precision = 2)
  private BigDecimal betUnitPrice;

  @Column(nullable = false, precision = 2)
  private BigDecimal winUnitPrice;

  private Integer maxBetItem;

  @Column(precision = 2)
  private BigDecimal betItemMaxAmt;

  @Column(precision = 2)
  private BigDecimal maxPayout;

  @Enumerated(EnumType.STRING)
  private LotteryStatus status = LotteryStatus.ACTIVE;

  public enum LotteryStatus {
    ACTIVE, SUSPENDED
  }
}

