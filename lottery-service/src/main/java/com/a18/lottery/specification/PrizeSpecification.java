package com.a18.lottery.specification;

import com.a18.lottery.model.Issue;
import com.a18.lottery.model.Prize;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

public class PrizeSpecification {
  public static Specification<Prize> filterPrize(
      Integer[] ids,
      Integer[] schedulerIds,
      Issue.IssueStatus status,
      String code
  ) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      cb.conjunction();

      if (ArrayUtils.isNotEmpty(ids)) {
        CriteriaBuilder.In<Integer> in = cb.in(root.get("id"));
        Arrays.stream(ids).forEach(in::value);
        predicates.add(in);
      }

      if (ArrayUtils.isNotEmpty(schedulerIds)) {
        CriteriaBuilder.In<Integer> in = cb.in(root.get("schedulerId"));
        Arrays.stream(schedulerIds).forEach(in::value);
        predicates.add(in);
      }

      if (StringUtils.isNotBlank(code)) {
        predicates.add(cb.like(
            cb.upper(root.get("code")),
            StringUtils.join("%", StringUtils.trimToEmpty(code).toUpperCase(), "%")
        ));
      }

      if (!Objects.isNull(status)) {
        predicates.add(cb.equal(root.get("status"), status));
      }
      return cb.and((predicates.toArray(new Predicate[0])));
    };
  }
}
