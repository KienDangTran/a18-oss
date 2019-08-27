package com.a18.account.specification;

import com.a18.account.model.Account;
import com.a18.account.model.AccountCategory;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.criteria.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

public class AccountSpecification {
  public static Specification<Account> filterAccount(
      String username,
      AccountCategory category,
      Account.AccountStatus status
  ) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      cb.conjunction();

      if (StringUtils.isNotBlank(username)) {
        predicates.add(cb.like(
            cb.lower(root.get("username")),
            StringUtils.join("%", StringUtils.trimToEmpty(username).toLowerCase(), "%")
        ));
      }

      if (!Objects.isNull(category)) {
        predicates.add(cb.equal(root.get("category"), category));
      }

      if (!Objects.isNull(status)) {
        predicates.add(cb.equal(root.get("status"), status));
      }

      return cb.and((predicates.toArray(new Predicate[0])));
    };
  }
}
