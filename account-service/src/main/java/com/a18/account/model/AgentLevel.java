package com.a18.account.model;

import com.a18.common.dto.BaseEntity;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
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
@ToString(exclude = {"commissions"})
@Entity
@Table(name = "agent_level", schema = "account")
public class AgentLevel extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, unique = true, updatable = false)
  private String code;

  private Integer superiorLevelId;

  @Column(nullable = false)
  private Integer minRequiredUser;

  @OneToMany(mappedBy = "agentLevel", cascade = CascadeType.ALL)
  private Set<Commission> commissions = new HashSet<>();
}
