package com.a18.lottery.model;

import com.a18.common.dto.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * <pre>
 * Note on {@link #betItems}:
 * - contains all bet item contents that were placed with same bet unit, separated by {@link #ITEM_CONTENTS_DELIMITER}.
 * - each bet item's content contain one or more bet numbers, separated by {@link #BET_NUMBERS_DELIMITER}
 *   base on configuration in {@link LotterySchema}:
 *  + number of bet number == {@link LotterySchema#getBetItemSize()}
 *  + number of digit of each bet number == {@link LotterySchema#getBetNoLength()}
 * - {@link #betItems} can contain 3 type of bet item's content
 *  + an individual bet item's content. e.g 126 / 12-34-56
 *  + a range (only use for lottery schema that has 1 number per bet item {@link LotterySchema#getBetItemSize()} == 1). e.g 00:99
 *  + a map with key is check position and value is a digit. e.g [{}]
 * </pre>
 */
@Data
@EqualsAndHashCode(exclude = "ticket")
@ToString(exclude = {"ticket"})
@Entity
@Table(name = "bet_item_group", schema = "lottery")
public class BetItemGroup extends BaseEntity {

  public static final String ITEM_CONTENTS_DELIMITER = ",";

  public static final String BET_NUMBERS_DELIMITER = "-";

  public static final String RANGE_SYMBOL = ":";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @JsonIgnore
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private Ticket ticket;

  @Column(nullable = false)
  private String betItems;

  @Column(nullable = false)
  private Integer betUnit;
}

