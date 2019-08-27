package com.a18.payment.specification;

import com.a18.payment.model.Bank;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.criteria.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

public class BankSpecification {
  public static Specification<Bank> filterBank(String name, Bank.BankStatus status) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      cb.conjunction();
      if (StringUtils.isNotBlank(name)) {
        predicates.add(cb.like(
            cb.lower(root.get("name")),
            StringUtils.join("%", StringUtils.trimToEmpty(name).toLowerCase(), "%")
        ));
      }

      if (!Objects.isNull(status)) {
        predicates.add(cb.equal(root.get("status"), status));
      }

      return cb.and((predicates.toArray(new Predicate[0])));
    };
  }
}
