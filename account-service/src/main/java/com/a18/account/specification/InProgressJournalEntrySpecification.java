package com.a18.account.specification;

import com.a18.account.model.Account;
import com.a18.account.model.Balance;
import com.a18.account.model.InProgressJournal;
import com.a18.common.constant.Ccy;
import com.a18.common.constant.GameCategory;
import com.a18.common.constant.Journal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

public class InProgressJournalEntrySpecification {
  public static Specification<InProgressJournal> filterInProgressJournalEntry(
      String username,
      Journal journal,
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

      Join<InProgressJournal, Balance> balanceJoin = root.join("balance");
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

      if (!Objects.isNull(journal)) {
        predicates.add(cb.equal(root.get("journal"), journal));
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
