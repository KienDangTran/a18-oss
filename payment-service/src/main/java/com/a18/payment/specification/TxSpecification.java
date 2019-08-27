package com.a18.payment.specification;

import com.a18.common.constant.Ccy;
import com.a18.common.constant.GameCategory;
import com.a18.common.constant.Journal;
import com.a18.payment.model.PaymentChannel;
import com.a18.payment.model.Tx;
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

public class TxSpecification {
  public static Specification<Tx> filterTx(
      Long[] ids, String username,
      Journal journal,
      GameCategory gameCategory,
      Ccy ccy,
      Integer bankId,
      Integer paymentMethodId,
      String remark,
      String invoiceNo,
      LocalDateTime createdDateGte,
      LocalDateTime createdDateLte
  ) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      cb.conjunction();

      if (ArrayUtils.isNotEmpty(ids)) {
        CriteriaBuilder.In<Long> in = cb.in(root.get("id"));
        Arrays.stream(ids).forEach(in::value);
        predicates.add(in);
      }

      if (StringUtils.isNotBlank(username)) {
        predicates.add(cb.like(
            cb.lower(root.get("username")),
            StringUtils.join("%", StringUtils.trimToEmpty(username).toLowerCase(), "%")
        ));
      }

      if (!Objects.isNull(journal)) {
        predicates.add(cb.equal(root.get("journal"), journal));
      }

      if (!Objects.isNull(gameCategory)) {
        predicates.add(cb.equal(root.get("gameCategory"), gameCategory));
      }

      if (!Objects.isNull(ccy)) {
        predicates.add(cb.equal(root.get("ccy"), ccy));
      }

      Join<Tx, PaymentChannel> paymentChannelJoin = root.join("paymentChannel");
      if (bankId != null) {
        predicates.add(cb.equal(paymentChannelJoin.get("bankId"), bankId));
      }

      if (paymentMethodId != null) {
        predicates.add(cb.equal(paymentChannelJoin.get("paymentMethodId"), paymentMethodId));
      }

      if (StringUtils.isNotBlank(remark)) {
        predicates.add(cb.like(
            cb.lower(root.get("remark")),
            StringUtils.join("%", StringUtils.trimToEmpty(remark).toLowerCase(), "%")
        ));
      }

      if (StringUtils.isNotBlank(invoiceNo)) {
        predicates.add(cb.like(
            cb.lower(root.get("remark")),
            StringUtils.join("%", StringUtils.trimToEmpty(invoiceNo).toLowerCase(), "%")
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
