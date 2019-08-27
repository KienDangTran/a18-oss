package com.a18.lottery.specification;

import com.a18.lottery.model.BetItemGroup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.persistence.criteria.Predicate;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

public class BetItemGroupSpecification {
  public static Specification<BetItemGroup> filterBetItemGroup(Long ticketId, String[] betItems) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      cb.conjunction();
      if (!Objects.isNull(ticketId)) {
        predicates.add(cb.equal(root.join("ticket").get("id"), ticketId));
      }

      if (ArrayUtils.isNotEmpty(betItems)) {
        predicates.add(cb.or(
            Arrays.stream(betItems)
                  .map(item -> cb.like(
                      root.get("betItems"),
                      StringUtils.join("%", StringUtils.trimToEmpty(item), "%")
                  ))
                  .toArray(Predicate[]::new)
        ));
      }

      return cb.and((predicates.toArray(new Predicate[0])));
    };
  }
}
