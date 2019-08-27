package com.a18.lottery.specification;

import com.a18.lottery.model.LotteryCategory;
import com.a18.lottery.model.Scheduler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

public class SchedulerSpecification {
  public static Specification<Scheduler> filterScheduler(
      Integer[] ids,
      LotteryCategory category,
      String code,
      String openDay,
      Scheduler.SchedulerStatus status
  ) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      cb.conjunction();

      if (ArrayUtils.isNotEmpty(ids)) {
        CriteriaBuilder.In<Integer> in = cb.in(root.get("id"));
        Arrays.stream(ids).forEach(in::value);
        predicates.add(in);
      }

      if (!Objects.isNull(category)) {
        predicates.add(cb.equal(root.get("category"), category));
      }

      if (StringUtils.isNotBlank(code)) {
        predicates.add(cb.like(
            cb.upper(root.get("code")),
            StringUtils.join("%", StringUtils.trimToEmpty(code).toUpperCase(), "%")
        ));
      }

      if (StringUtils.isNotBlank(openDay)) {
        predicates.add(cb.like(
            cb.upper(root.get("openDay")),
            StringUtils.join("%", StringUtils.trimToEmpty(openDay).toUpperCase(), "%")
        ));
      }

      if (!Objects.isNull(status)) {
        predicates.add(cb.equal(root.get("status"), status));
      }

      return cb.and((predicates.toArray(new Predicate[0])));
    };
  }
}
