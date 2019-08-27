package com.a18.account.specification;

import com.a18.account.model.Account;
import com.a18.account.model.Balance;
import com.a18.account.model.JournalEntry;
import com.a18.common.constant.Ccy;
import com.a18.common.constant.GameCategory;
import com.a18.common.constant.Journal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

public class JournalEntrySpecification {
  public static Specification<JournalEntry> filterJournalEntry(
      String username,
      Journal[] journals,
      GameCategory gameCategory,
      Ccy ccy,
      Long refId,
      String refType,
      LocalDateTime createdDateGte,
      LocalDateTime createdDateLte
  ) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      cb.conjunction();

      Join<JournalEntry, Balance> balanceJoin = root.join("balance");
      Join<Balance, Account> accountJoin = balanceJoin.join("account");
      predicates.add(cb.notEqual(cb.lower(accountJoin.get("username")), "root"));

      if (StringUtils.isNotBlank(username)) {
        predicates.add(cb.like(
            cb.lower(accountJoin.get("username")),
            StringUtils.join("%", StringUtils.trimToEmpty(username).toLowerCase(), "%")
        ));
      }

      if (!Objects.isNull(gameCategory)) {
        predicates.add(cb.equal(balanceJoin.get("gameCategory"), gameCategory));
      }

      if (!Objects.isNull(ccy)) {
        predicates.add(cb.equal(balanceJoin.get("ccy"), ccy));
      }

      if (ArrayUtils.isNotEmpty(journals)) {
        CriteriaBuilder.In<Journal> in = cb.in(root.get("journal"));
        Arrays.stream(journals).forEach(in::value);
        predicates.add(in);
      }

      if (!Objects.isNull(refId)) {
        predicates.add(cb.equal(root.get("refId"), refId));
      }

      if (StringUtils.isNotBlank(refType)) {
        predicates.add(cb.like(
            cb.upper(root.get("refType")),
            StringUtils.join("%", StringUtils.trimToEmpty(refType).toUpperCase(), "%")
        ));
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
