package com.a18.lottery.model;

import com.a18.common.constant.Ccy;
import com.a18.common.dto.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(of = {"issueId", "lotteryId", "ccy", "username", "betItemGroups"})
@ToString(exclude = {"betItemGroups", "lottery", "issue"})
@Entity
@Table(name = "ticket", schema = "lottery")
public class Ticket extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "issue_id", nullable = false, updatable = false)
  private Long issueId;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "issue_id", updatable = false, insertable = false)
  private Issue issue;

  @Column(name = "lottery_id", nullable = false, updatable = false)
  private Integer lotteryId;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "lottery_id", updatable = false, insertable = false)
  private Lottery lottery;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, updatable = false)
  private Ccy ccy;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Column(nullable = false, updatable = false)
  private String username;

  @JsonIgnore
  private String deviceRegistrationToken;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Integer totalBetItem = 0;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Integer totalBetUnit = 0;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private BigDecimal totalBetAmt = BigDecimal.ZERO;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Integer totalWonUnit = 0;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private BigDecimal totalPayout = BigDecimal.ZERO;

  @Enumerated(EnumType.STRING)
  private TicketStatus status = TicketStatus.NEW;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<BetItemGroup> betItemGroups = new HashSet<>();

  public enum TicketStatus {
    NEW, WON, LOST, PAYING, FAILED
  }
}
