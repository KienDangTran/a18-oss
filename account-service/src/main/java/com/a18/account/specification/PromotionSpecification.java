package com.a18.account.specification;

import com.a18.account.model.Promotion;
import com.a18.common.constant.Ccy;
import com.a18.common.constant.GameCategory;
import com.a18.common.constant.Journal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

public class PromotionSpecification {
  public static Specification<Promotion> filterPromotion(
      Integer[] ids,
      String code,
      Boolean autoApply,
      Journal journal,
      Ccy ccy,
      GameCategory gameCategory,
      String description,
      Promotion.PromotionStatus status
  ) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      cb.conjunction();

      if (ArrayUtils.isNotEmpty(ids)) {
        CriteriaBuilder.In<Integer> in = cb.in(root.get("id"));
        Arrays.stream(ids).forEach(in::value);
        predicates.add(in);
      }

      if (StringUtils.isNotBlank(code)) {
        predicates.add(cb.like(
            cb.upper(root.get("code")),
            StringUtils.join("%", StringUtils.trimToEmpty(code).toUpperCase(), "%")
        ));
      }

      if (!Objects.isNull(autoApply)) {
        predicates.add(cb.equal(root.get("autoApply"), autoApply));
      }

      if (!Objects.isNull(gameCategory)) {
        predicates.add(cb.equal(root.get("gameCategory"), gameCategory));
      }

      if (!Objects.isNull(ccy)) {
        predicates.add(cb.equal(root.get("ccy"), ccy));
      }

      if (!Objects.isNull(journal)) {
        predicates.add(cb.equal(root.get("journal"), journal));
      }

      if (StringUtils.isNotBlank(description)) {
        predicates.add(cb.like(
            cb.lower(root.get("description")),
            StringUtils.join("%", StringUtils.trimToEmpty(description).toLowerCase(), "%")
        ));
      }

      if (!Objects.isNull(status)) {
        predicates.add(cb.equal(root.get("journal"), status));
      }

      return cb.and((predicates.toArray(new Predicate[0])));
    };
  }
}
