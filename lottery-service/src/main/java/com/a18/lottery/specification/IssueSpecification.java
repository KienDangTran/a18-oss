package com.a18.lottery.specification;

import com.a18.lottery.model.Issue;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

public class IssueSpecification {
  public static Specification<Issue> filterIssue(
      Long[] ids,
      Integer[] schedulerIds,
      Issue.IssueStatus[] statuses,
      String code,
      LocalDateTime openingTimeGte,
      LocalDateTime openingTimeLte
  ) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      cb.conjunction();

      if (ArrayUtils.isNotEmpty(ids)) {
        CriteriaBuilder.In<Long> in = cb.in(root.get("id"));
        Arrays.stream(ids).forEach(in::value);
        predicates.add(in);
      }

      if (ArrayUtils.isNotEmpty(schedulerIds)) {
        CriteriaBuilder.In<Integer> in = cb.in(root.get("schedulerId"));
        Arrays.stream(schedulerIds).forEach(in::value);
        predicates.add(in);
      }

      if (ArrayUtils.isNotEmpty(statuses)) {
        CriteriaBuilder.In<Issue.IssueStatus> in = cb.in(root.get("status"));
        Arrays.stream(statuses).forEach(in::value);
        predicates.add(in);
      }

      if (StringUtils.isNotBlank(code)) {
        predicates.add(cb.like(
            cb.upper(root.get("code")),
            StringUtils.join("%", StringUtils.trimToEmpty(code).toUpperCase(), "%")
        ));
      }

      if (openingTimeGte != null) {
        predicates.add(cb.greaterThan(root.get("openingTime"), openingTimeGte));
      }

      if (openingTimeLte != null) {
        predicates.add(cb.lessThan(root.get("openingTime"), openingTimeLte));
      }
      return cb.and((predicates.toArray(new Predicate[0])));
    };
  }
}
