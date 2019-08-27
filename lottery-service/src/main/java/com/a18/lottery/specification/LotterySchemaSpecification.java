package com.a18.lottery.specification;

import com.a18.lottery.model.LotterySchema;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

public class LotterySchemaSpecification {
  public static Specification<LotterySchema> filterLotterySchema(Long[] ids, String code) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      cb.conjunction();
      if (ArrayUtils.isNotEmpty(ids)) {
        CriteriaBuilder.In<Long> in = cb.in(root.get("id"));
        Arrays.stream(ids).forEach(in::value);
        predicates.add(in);
      }

      if (StringUtils.isNotBlank(code)) {
        predicates.add(cb.like(
            cb.upper(root.get("code")),
            StringUtils.join("%", StringUtils.trimToEmpty(code).toUpperCase(), "%")
        ));
      }

      return cb.and((predicates.toArray(new Predicate[0])));
    };
  }
}
