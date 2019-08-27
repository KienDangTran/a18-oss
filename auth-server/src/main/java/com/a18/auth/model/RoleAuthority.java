package com.a18.auth.model;

import com.a18.common.dto.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(exclude = {"authority", "role"})
@ToString(exclude = {"authority", "role"})
@Entity
@Table(name = "role_authority", schema = "auth")
public class RoleAuthority extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "authority_id", nullable = false)
  private Integer authorityId;

  @JsonIgnore
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "authority_id", insertable = false, updatable = false)
  private Authority authority;

  @JsonIgnore
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private Role role;

  private boolean read = true;

  private boolean write = false;

  private boolean exec = false;
}
