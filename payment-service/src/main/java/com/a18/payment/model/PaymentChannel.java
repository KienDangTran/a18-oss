package com.a18.payment.model;

import com.a18.common.constant.Ccy;
import com.a18.common.dto.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
@EqualsAndHashCode(of = {"paymentVendorId", "paymentMethodId", "bankId"})
@ToString(exclude = {"paymentVendor", "paymentMethod", "bank"})
@Entity
@Table(name = "payment_channel", schema = "payment")
public class PaymentChannel extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "payment_vendor_id", insertable = false, updatable = false, nullable = false)
  private Integer paymentVendorId;

  @JsonIgnore
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private PaymentVendor paymentVendor;

  @Column(name = "payment_method_id", insertable = false, updatable = false, nullable = false)
  private Integer paymentMethodId;

  @JsonIgnore
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private PaymentMethod paymentMethod;

  @Column(name = "bank_id", insertable = false, updatable = false, nullable = false)
  private Integer bankId;

  @JsonIgnore
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private Bank bank;

  @Enumerated(EnumType.STRING)
  private Ccy ccy;

  @Column(nullable = false, precision = 2)
  private BigDecimal minAmt;

  @Column(precision = 2)
  private BigDecimal maxAmt;

  @Enumerated(EnumType.STRING)
  private PaymentCardType requiredCardType;

  private Boolean bankAccountRequired;

  private Boolean deposit = true;

  private Boolean withdrawal = false;

  private Boolean autoApprove = false;

  //public String getPaymentVendorCode() {
  //  return this.paymentVendor.getCode();
  //}
  //
  //public String getBankCode() {
  //  return this.bank.getCode();
  //}
  //
  //public String getBankName() {
  //  return this.bank.getName();
  //}
  //
  //public String getPaymentMethodCode() {
  //  return this.paymentMethod.getCode();
  //}
  //
  //public String getPaymentMethodName() {
  //  return this.paymentMethod.getName();
  //}
}
