package com.a18.payment.specification;

import com.a18.common.constant.Ccy;
import com.a18.payment.model.PaymentCard;
import com.a18.payment.model.PaymentCardType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.criteria.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

public class PaymentCardSpecification {
  public static Specification<PaymentCard> filterPaymentCard(
      String username,
      Ccy ccy,
      PaymentCardType type,
      Integer bankId,
      String bankAccount,
      String cardNo,
      String ownerName
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

      if (!Objects.isNull(ccy)) {
        predicates.add(cb.equal(root.get("ccy"), ccy));
      }

      if (!Objects.isNull(type)) {
        predicates.add(cb.equal(root.get("type"), type));
      }

      if (!Objects.isNull(bankId)) {
        predicates.add(cb.equal(root.get("bankId"), bankId));
      }

      if (StringUtils.isNotBlank(bankAccount)) {
        predicates.add(cb.like(
            cb.upper(root.get("remark")),
            StringUtils.join("%", StringUtils.trimToEmpty(bankAccount).toUpperCase(), "%")
        ));
      }

      if (StringUtils.isNotBlank(cardNo)) {
        predicates.add(cb.like(
            cb.upper(root.get("cardNo")),
            StringUtils.join("%", StringUtils.trimToEmpty(cardNo).toUpperCase(), "%")
        ));
      }

      if (StringUtils.isNotBlank(ownerName)) {
        predicates.add(cb.like(
            cb.upper(root.get("ownerName")),
            StringUtils.join("%", StringUtils.trimToEmpty(ownerName).toUpperCase(), "%")
        ));
      }

      return cb.and((predicates.toArray(new Predicate[0])));
    };
  }
}
