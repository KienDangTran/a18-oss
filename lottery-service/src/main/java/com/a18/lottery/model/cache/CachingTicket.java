package com.a18.lottery.model.cache;

import com.a18.common.constant.Ccy;
import com.a18.lottery.model.BetItemGroup;
import com.a18.lottery.model.dto.BetItemGroupDTO;
import com.a18.lottery.model.dto.TicketDTO;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;
import javax.persistence.Id;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@Data
@NoArgsConstructor
@EqualsAndHashCode(of = {"issueId", "lotteryId", "ccy", "username"})
@ToString(exclude = {"betItemGroups"})
@RedisHash("ticket")
public class CachingTicket implements Serializable {

  @NotNull @Id private Long id;

  @NotNull @Indexed private Long issueId;

  @NotNull @Indexed private Integer lotteryId;

  @NotNull @Indexed private Ccy ccy;

  @NotNull @Indexed private String username;

  @NotNull private BigDecimal betUnitPrice;

  @NotNull private Integer lotterySchemaId;

  @NotEmpty private Map<Integer, String> betItemGroups;

  public CachingTicket(TicketDTO ticketDTO, BigDecimal betUnitPrice, Integer lotterySchemaId) {
    this.id = ticketDTO.getId();
    this.issueId = ticketDTO.getIssueId();
    this.lotteryId = ticketDTO.getLotteryId();
    this.username = ticketDTO.getUsername();
    this.betItemGroups =
        ticketDTO.getBetItemGroups()
                 .stream()
                 .collect(Collectors.toMap(
                     BetItemGroupDTO::getBetUnit,
                     BetItemGroupDTO::getBetItems,
                     (s, s2) -> StringUtils.joinWith(BetItemGroup.ITEM_CONTENTS_DELIMITER, s, s2)
                 ));
    this.betUnitPrice = betUnitPrice;
    this.lotterySchemaId = lotterySchemaId;
  }
}
