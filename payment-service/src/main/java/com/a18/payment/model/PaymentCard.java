package com.a18.payment.model;

import com.a18.common.constant.Ccy;
import com.a18.common.dto.BaseEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
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
import lombok.ToString;
import org.hibernate.annotations.ColumnTransformer;

@Data
@EqualsAndHashCode(of = {"username", "bankId", "type", "ccy"})
@ToString(exclude = {"cvv"})
@Entity
@Table(name = "payment_card", schema = "payment")
public class PaymentCard extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Column(nullable = false, updatable = false)
  private String username;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Ccy ccy;

  @Column(nullable = false)
  private Integer bankId;

  @Column(updatable = false)
  private String bankAccount;

  private String branch;

  @Column(updatable = false)
  private String cardNo;

  @Column(nullable = false, updatable = false)
  @ColumnTransformer(read = "UPPER(owner_name)", write = "UPPER(?)")
  private String ownerName;

  @Enumerated(EnumType.STRING)
  private PaymentCardType type;

  private Integer cardMonth;

  private Integer cardYear;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private String cvv;

  @Enumerated(EnumType.STRING)
  private PaymentCardStatus status = PaymentCardStatus.ACTIVE;

  public enum PaymentCardStatus {
    ACTIVE, SUSPENDED, LOCKED
  }
}
