package com.a18.account.model;

import com.a18.common.dto.BaseEntity;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
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
@EqualsAndHashCode(of = {"agentLevel", "companyRevenueFrom", "companyRevenueTo"})
@ToString(exclude = {"agentLevel"})
@Entity
@Table(name = "commission", schema = "account")
public class Commission extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private AgentLevel agentLevel;

  @Column(nullable = false, precision = 2)
  private BigDecimal companyRevenueFrom;

  @Column(precision = 2)
  private BigDecimal companyRevenueTo;

  @Column(nullable = false, precision = 2)
  private BigDecimal rate;
}
