package com.a18.payment.model;

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
@Table(name = "bank", schema = "payment")
public class Bank extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, unique = true, updatable = false)
  private String code;

  private String name;

  private String logoUrl;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private BankType type;

  @Enumerated(EnumType.STRING)
  private BankStatus status = BankStatus.ACTIVE;

  public enum BankType {
    LOCAL_BANK, GLOBAL_BANK, E_WALLET
  }

  public enum BankStatus {
    ACTIVE, SUSPENDED
  }
}
