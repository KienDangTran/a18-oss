package com.a18.lottery.model;

import com.a18.common.dto.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

@Data
@EqualsAndHashCode(of = {"code"})
@ToString(exclude = {"scheduler", "prizeSchema"})
@Entity
@Table(name = "prize", schema = "lottery")
public class Prize extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, unique = true, updatable = false)
  private String code;

  @Column(name = "prize_schema_id", insertable = false, updatable = false, nullable = false)
  private Integer prizeSchemaId;

  @JsonIgnore
  @ManyToOne(optional = false)
  private PrizeSchema prizeSchema;

  @Column(name = "scheduler_id", insertable = false, updatable = false, nullable = false)
  private Integer schedulerId;

  @Column(nullable = false)
  private Integer prizePosition;

  @Enumerated(EnumType.STRING)
  private PrizeStatus status = PrizeStatus.ACTIVE;

  public enum PrizeStatus {
    ACTIVE, SUSPENDED
  }
}

