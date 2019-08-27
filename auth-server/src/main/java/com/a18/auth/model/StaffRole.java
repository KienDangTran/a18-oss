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
@EqualsAndHashCode(exclude = {"staff", "role"})
@ToString(exclude = {"staff", "role"})
@Entity
@Table(name = "staff_role", schema = "auth")
public class StaffRole extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "role_id", nullable = false)
  private Integer roleId;

  @JsonIgnore
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "role_id", insertable = false, updatable = false)
  private Role role;

  @JsonIgnore
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private Staff staff;
}
