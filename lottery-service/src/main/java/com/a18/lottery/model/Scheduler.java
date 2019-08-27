package com.a18.lottery.model;

import com.a18.common.dto.BaseEntity;
import java.time.LocalTime;
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
@ToString(exclude = {"lotteries", "prizes"})
@Entity
@Table(name = "scheduler", schema = "lottery")
public class Scheduler extends BaseEntity {

  public static final String RSS_SRC_DELIMITER = ",";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, unique = true, updatable = false)
  private String code;

  @Enumerated(EnumType.STRING)
  private LotteryCategory category;

  private String openDay;

  private LocalTime startAt;

  @Column(nullable = false)
  private Integer openDuration;

  @Column(nullable = false)
  private Integer idleDuration;

  private String drawResultSrc;

  private String logoUrl;

  @Enumerated(EnumType.STRING)
  private SchedulerStatus status = SchedulerStatus.ACTIVE;

  public enum SchedulerStatus {
    ACTIVE, SUSPENDED
  }
}
