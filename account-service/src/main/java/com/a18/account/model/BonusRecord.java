package com.a18.account.model;

import com.a18.common.dto.BaseEntity;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(of = {"balance", "promotion"})
@ToString(exclude = {"balance", "promotion"})
@Entity
@Table(name = "bonus_record", schema = "account")
public class BonusRecord extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "promotion_id", updatable = false, nullable = false)
  private Integer promotionId;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "promotion_id", insertable = false, updatable = false)
  private Promotion promotion;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private Balance balance;

  @Column(nullable = false)
  private Integer appliedCount = 0;

  @Column(nullable = false, precision = 2)
  private BigDecimal bonusAmt = BigDecimal.ZERO;
}
