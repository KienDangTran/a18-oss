package com.a18.auth.model;

import com.a18.common.dto.BaseEntity;
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
import javax.persistence.OrderBy;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(of = {"code", "roleAuthorities"})
@ToString(exclude = {"roleAuthorities"})
@Entity
@Table(name = "role", schema = "auth")
public class Role extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, unique = true, updatable = false)
  private String code;

  private Integer parentRoleId;

  @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("authorityId")
  private Set<RoleAuthority> roleAuthorities = new HashSet<>();

  @Enumerated(EnumType.STRING)
  private RoleStatus status = RoleStatus.ACTIVE;

  public enum RoleStatus {
    ACTIVE, SUSPENDED
  }
}
