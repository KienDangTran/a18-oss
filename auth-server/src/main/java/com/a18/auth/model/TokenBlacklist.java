package com.a18.auth.model;

import com.a18.common.dto.BaseEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "token_blacklist", schema = "auth")
public class TokenBlacklist extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String jti;

  @Column(nullable = false)
  private String username;

  private Long expiresIn;

  private Boolean blacklisted;
}

