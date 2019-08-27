package com.a18.lottery.specification;

import com.a18.lottery.model.DrawResult;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.data.jpa.domain.Specification;

public class DrawResultSpecification {
  public static Specification<DrawResult> filterDrawResult(
      Long[] ids,
      Long[] issueIds,
      Integer[] prizeIds,
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
        CriteriaBuilder.In<Long> in = cb.in(root.get("issueId"));
        Arrays.stream(issueIds).forEach(in::value);
        predicates.add(in);
      }

      if (ArrayUtils.isNotEmpty(prizeIds)) {
        CriteriaBuilder.In<Integer> in = cb.in(root.get("prizeId"));
        Arrays.stream(prizeIds).forEach(in::value);
        predicates.add(in);
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
