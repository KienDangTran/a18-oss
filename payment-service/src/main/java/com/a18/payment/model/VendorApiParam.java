package com.a18.payment.model;

import com.a18.common.dto.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
@EqualsAndHashCode(of = {"paymentVendor", "key"})
@ToString(exclude = {"paymentVendor"})
@Entity
@Table(name = "vendor_api_param", schema = "payment")
public class VendorApiParam extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @JsonIgnore
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private PaymentVendor paymentVendor;

  @Column(nullable = false)
  private String key;

  private String defaultValue;

  private Boolean required;

  private String description;
}
