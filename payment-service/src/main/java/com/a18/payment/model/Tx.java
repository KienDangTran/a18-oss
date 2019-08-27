package com.a18.payment.model;

import com.a18.common.constant.GameCategory;
import com.a18.common.constant.Journal;
import com.a18.common.dto.BaseEntity;
import com.a18.payment.model.dto.TxDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@NoArgsConstructor
@EqualsAndHashCode(of = {"code"})
@Entity
@Table(name = "tx", schema = "payment")
public class Tx extends BaseEntity {

  public Tx(TxDTO txDTO) {
    if (StringUtils.isNotBlank(txDTO.getCode())) this.code = txDTO.getCode();
    if (StringUtils.isNotBlank(txDTO.getUsername())) this.username = txDTO.getUsername();
    if (!Objects.isNull(txDTO.getPaymentChannelId())) {
      this.paymentChannelId = txDTO.getPaymentChannelId();
    }
    if (!Objects.isNull(txDTO.getPaymentChannel())) {
      this.paymentChannel = txDTO.getPaymentChannel();
    }
    if (!Objects.isNull(txDTO.getJournal())) this.journal = txDTO.getJournal();
    if (!Objects.isNull(txDTO.getGameCategory())) this.gameCategory = txDTO.getGameCategory();
    if (!Objects.isNull(txDTO.getAmt())) this.amt = txDTO.getAmt();
    if (!Objects.isNull(txDTO.getStatus())) this.status = txDTO.getStatus();
    if (StringUtils.isNotBlank(txDTO.getToken())) this.token = txDTO.getToken();
    if (StringUtils.isNotBlank(txDTO.getCheckoutUrl())) this.checkoutUrl = txDTO.getCheckoutUrl();
    if (StringUtils.isNotBlank(txDTO.getErrorCode())) this.errorCode = txDTO.getErrorCode();
    if (StringUtils.isNotBlank(txDTO.getInvoiceNo())) this.invoiceNo = txDTO.getInvoiceNo();
    if (StringUtils.isNotBlank(txDTO.getRemark())) this.remark = txDTO.getRemark();
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Column(nullable = false, updatable = false, unique = true)
  private String code;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Column(nullable = false, updatable = false, unique = true)
  private String username;

  @Column(name = "payment_channel_id", nullable = false, updatable = false)
  private Integer paymentChannelId;

  @JsonIgnore
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "payment_channel_id", updatable = false, insertable = false)
  private PaymentChannel paymentChannel;

  @Column(nullable = false, updatable = false)
  @Enumerated(EnumType.STRING)
  private Journal journal;

  @Column(nullable = false, updatable = false)
  @Enumerated(EnumType.STRING)
  private GameCategory gameCategory;

  private BigDecimal exchangeRate;

  @Column(nullable = false, precision = 2)
  private BigDecimal amt = BigDecimal.ZERO;

  @Column(precision = 2)
  private BigDecimal fee = BigDecimal.ZERO;

  private String token;

  private String errorCode;

  private String invoiceNo;

  private String remark;

  private String checkoutUrl;

  @Enumerated(EnumType.STRING)
  private TxStatus status = TxStatus.NEW;

  public enum TxStatus {
    NEW, PENDING, IN_PROGRESS, SUCCESS, REJECTED, FAILED
  }
}
