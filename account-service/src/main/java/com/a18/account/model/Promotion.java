package com.a18.account.model;

import com.a18.common.constant.Ccy;
import com.a18.common.constant.GameCategory;
import com.a18.common.constant.Journal;
import com.a18.common.dto.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
@EqualsAndHashCode(of = {"code"})
@Entity
@Table(name = "promotion", schema = "account")
public class Promotion extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, unique = true, updatable = false)
  private String code;

  private Boolean autoApply = false;

  @Enumerated(EnumType.STRING)
  private AdjustType adjustType = AdjustType.AMT;

  @Column(nullable = false, updatable = false)
  @Enumerated(EnumType.STRING)
  private Journal journal;

  @Column(nullable = false, updatable = false)
  @Enumerated(EnumType.STRING)
  private Ccy ccy;

  @Column(nullable = false, updatable = false)
  @Enumerated(EnumType.STRING)
  private GameCategory gameCategory;

  private String description;

  @Column(nullable = false, precision = 2)
  private BigDecimal bonusValue = BigDecimal.ZERO;

  @Column(nullable = false, precision = 2)
  private BigDecimal turnoverFactor;

  private Integer maxApplyTime;

  @Column(nullable = false)
  private LocalDateTime startTime;

  private LocalDateTime endTime;

  private Long expireIn;

  @Enumerated(EnumType.STRING)
  private PromotionStatus status = PromotionStatus.NEW;

  public enum AdjustType {
    AMT, PERCENT
  }

  public enum PromotionStatus {
    NEW, APPLYING, ENDED
  }
}
