package com.a18.lottery.specification;

import com.a18.common.constant.Ccy;
import com.a18.lottery.model.Issue;
import com.a18.lottery.model.Lottery;
import com.a18.lottery.model.Ticket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

public class TicketSpecification {
  public static Specification<Ticket> filterTicket(
      Long[] ids,
      Integer[] issueIds,
      Integer[] schedulerIds,
      Integer[] lotterySchemaIds,
      Ticket.TicketStatus[] statuses,
      Ccy ccy,
      String username,
      LocalDateTime createdDateGte,
      LocalDateTime createdDateLte
  ) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      cb.conjunction();

      if (ArrayUtils.isNotEmpty(ids)) {
        CriteriaBuilder.In<Long> in = cb.in(root.get("id"));
        Arrays.stream(ids).forEach(in::value);
        predicates.add(in);
      }

      if (ArrayUtils.isNotEmpty(issueIds)) {
        CriteriaBuilder.In<Integer> in = cb.in(root.get("issueId"));
        Arrays.stream(issueIds).forEach(in::value);
        predicates.add(in);
      }

      if (ArrayUtils.isNotEmpty(schedulerIds)) {
        Join<Ticket, Issue> ticketIssueJoin = root.join("issue");
        CriteriaBuilder.In<Integer> in = cb.in(ticketIssueJoin.get("schedulerId"));
        Arrays.stream(schedulerIds).forEach(in::value);
        predicates.add(in);
      }

      if (ArrayUtils.isNotEmpty(lotterySchemaIds)) {
        Join<Ticket, Lottery> ticketLotteryJoin = root.join("lottery");
        CriteriaBuilder.In<Integer> in = cb.in(ticketLotteryJoin.get("lotterySchemaId"));
        Arrays.stream(lotterySchemaIds).forEach(in::value);
        predicates.add(in);
      }

      if (ArrayUtils.isNotEmpty(statuses)) {
        CriteriaBuilder.In<Ticket.TicketStatus> in = cb.in(root.get("status"));
        Arrays.stream(statuses).forEach(in::value);
        predicates.add(in);
      }

      if (StringUtils.isNotBlank(username)) {
        predicates.add(cb.like(
            cb.lower(root.get("username")),
            StringUtils.join("%", StringUtils.trimToEmpty(username).toLowerCase(), "%")
        ));
      }

      if (!Objects.isNull(ccy)) {
        predicates.add(cb.equal(root.get("ccy"), ccy));
      }

      if (createdDateGte != null) {
        predicates.add(cb.greaterThan(root.get("createdDate"), createdDateGte));
      }

      if (createdDateLte != null) {
        predicates.add(cb.lessThan(root.get("createdDate"), createdDateLte));
      }

      return cb.and((predicates.toArray(new Predicate[0])));
    };
  }
}
