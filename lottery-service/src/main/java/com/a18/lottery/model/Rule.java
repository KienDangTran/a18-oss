package com.a18.lottery.model;

import com.a18.common.dto.BaseEntity;
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
@Table(name = "rule", schema = "lottery")
public class Rule extends BaseEntity {
  public static final String CHECK_POSITION_DELIMITER = ",";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, unique = true, updatable = false)
  private String code;

  @Column(nullable = false, updatable = false)
  private Integer prizeSchemasId;

  @Column(nullable = false, updatable = false)
  private Integer lotterySchemaId;

  @Column(nullable = false)
  private Boolean checkMatch = true;

  @Column(nullable = false)
  private String checkPositions;

  @Enumerated(EnumType.STRING)
  private RuleStatus status = RuleStatus.ACTIVE;

  public enum RuleStatus {
    ACTIVE, SUSPENDED
  }
}
