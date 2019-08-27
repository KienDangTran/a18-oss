package com.a18.payment.model;

import com.a18.common.dto.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(of = {"code"})
@ToString(exclude = {"vendorApiParams"})
@Entity
@Table(name = "payment_vendor", schema = "payment")
public class PaymentVendor extends BaseEntity {
  public static final String NGAN_LUONG = "NGAN_LUONG";

  public static final String HELP_2_PAY = "HELP_2_PAY";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, unique = true, updatable = false)
  private String code;

  @Column(nullable = false)
  private String depositUri;

  @Column(nullable = false)
  private String withdrawalUri;

  @JsonIgnore
  @OneToMany(mappedBy = "paymentVendor", cascade = CascadeType.ALL)
  private Set<VendorApiParam> vendorApiParams = new HashSet<>();

  @Enumerated(EnumType.STRING)
  private PaymentVendorStatus status = PaymentVendorStatus.ACTIVE;

  public enum PaymentVendorStatus {
    ACTIVE, SUSPENDED
  }
}
