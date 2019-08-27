package com.a18.lottery.model;

import com.a18.common.dto.BaseEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "won_item", schema = "lottery")
public class WonItem extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Column(nullable = false, updatable = false)
  private Long ticketId;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Column(nullable = false)
  private String betContent;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Integer wonCount;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Column(nullable = false)
  private Integer wonUnit;
}

