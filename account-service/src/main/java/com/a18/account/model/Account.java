package com.a18.account.model;

import com.a18.common.dto.BaseEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@EqualsAndHashCode(of = {"username", "category"})
@ToString(exclude = {"balances"})
@Entity
@Table(name = "account", schema = "account")
public class Account extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Column(nullable = false)
  private String username;

  @Enumerated(EnumType.STRING)
  private AccountCategory category = AccountCategory.USER_ASSET;

  private String accountName;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Long superiorAccountId;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Integer agentLevelId;

  @Enumerated(EnumType.STRING)
  private AccountStatus status = AccountStatus.ACTIVE;

  @OneToMany(mappedBy = "account", cascade = {CascadeType.REFRESH, CascadeType.MERGE})
  private Set<Balance> balances = new HashSet<>();

  public enum AccountStatus {
    ACTIVE, SUSPENDED, LOCKED
  }
}
