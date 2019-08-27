package com.a18.auth.specification;

import com.a18.auth.model.User;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {
  public static Specification<User> filterUser(
      String username,
      String fullname,
      String email,
      String phone,
      Boolean enabled
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

      if (StringUtils.isNotBlank(fullname)) {
        predicates.add(cb.like(
            cb.upper(root.get("fullname")),
            StringUtils.join("%", StringUtils.trimToNull(fullname).toUpperCase(), "%")
        ));
      }

      if (StringUtils.isNotBlank(email)) {
        predicates.add(cb.like(
            cb.lower(root.get("email")),
            StringUtils.join("%", StringUtils.trimToEmpty(email), "%")
        ));
      }

      if (StringUtils.isNotBlank(phone)) {
        predicates.add(cb.like(
            cb.lower(root.get("phone")),
            StringUtils.join("%", StringUtils.trimToEmpty(phone), "%")
        ));
      }

      if (enabled != null) {
        predicates.add(cb.equal(root.get("enabled"), enabled));
      }

      return cb.and((predicates.toArray(new Predicate[0])));
    };
  }
}
