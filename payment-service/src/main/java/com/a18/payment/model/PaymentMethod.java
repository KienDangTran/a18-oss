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
@Table(name = "payment_method", schema = "payment")
public class PaymentMethod extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, unique = true, updatable = false)
  private String code;

  @Column(unique = true)
  private String name;

  @Enumerated(EnumType.STRING)
  private PaymentMethodStatus status = PaymentMethodStatus.ACTIVE;

  public enum PaymentMethodStatus {
    ACTIVE, SUSPENDED
  }
}
