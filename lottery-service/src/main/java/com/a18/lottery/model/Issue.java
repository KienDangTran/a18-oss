package com.a18.lottery.model;

import com.a18.common.dto.BaseEntity;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(of = {"code"})
@ToString(exclude = {"scheduler", "drawResults"})
@Entity
@Table(name = "issue", schema = "lottery")
public class Issue extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, updatable = false)
  private String code;

  @Column(name = "scheduler_id", nullable = false, updatable = false)
  private Integer schedulerId;

  @Column(nullable = false, updatable = false)
  private LocalDateTime openingTime;

  private LocalDateTime actualOpeningTime;

  @Column(nullable = false, updatable = false)
  private LocalDateTime closingTime;

  private LocalDateTime actualClosingTime;

  private LocalDateTime endingTime;

  @Enumerated(EnumType.STRING)
  private IssueStatus status = IssueStatus.NEW;

  public enum IssueStatus {
    NEW, OPENING, CLOSED, ENDED
  }
}
