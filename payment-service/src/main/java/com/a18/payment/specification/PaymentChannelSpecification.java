package com.a18.payment.specification;

import com.a18.common.constant.Ccy;
import com.a18.payment.model.PaymentCardType;
import com.a18.payment.model.PaymentChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.data.jpa.domain.Specification;

public class PaymentChannelSpecification {
  public static Specification<PaymentChannel> filterPaymentChannel(
      Integer[] ids,
      Integer paymentVendorId,
      Integer bankId,
      Integer paymentMethodId,
      Ccy ccy,
      Boolean bankAccountRequired,
      PaymentCardType requiredCardType,
      Boolean deposit,
      Boolean withdrawal,
      Boolean autoApprove
  ) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      cb.conjunction();
      if (ArrayUtils.isNotEmpty(ids)) {
        CriteriaBuilder.In<Integer> in = cb.in(root.get("id"));
        Arrays.stream(ids).forEach(in::value);
        predicates.add(in);
      }
      if (!Objects.isNull(paymentVendorId)) {
        predicates.add(cb.equal(root.get("paymentVendorId"), paymentVendorId));
      }
      if (!Objects.isNull(bankId)) {
        predicates.add(cb.equal(root.get("bankId"), bankId));
      }
      if (!Objects.isNull(paymentMethodId)) {
        predicates.add(cb.equal(root.get("paymentMethodId"), paymentMethodId));
      }
      if (!Objects.isNull(ccy)) {
        predicates.add(cb.equal(root.get("ccy"), ccy));
      }
      if (!Objects.isNull(bankAccountRequired)) {
        predicates.add(cb.equal(root.get("bankAccountRequired"), bankAccountRequired));
      }
      if (!Objects.isNull(requiredCardType)) {
        predicates.add(cb.equal(root.get("requiredCardType"), requiredCardType));
      }
      if (!Objects.isNull(deposit)) {
        predicates.add(cb.equal(root.get("deposit"), deposit));
      }
      if (!Objects.isNull(withdrawal)) {
        predicates.add(cb.equal(root.get("withdrawal"), withdrawal));
      }
      if (!Objects.isNull(autoApprove)) {
        predicates.add(cb.equal(root.get("autoApprove"), autoApprove));
      }

      return cb.and((predicates.toArray(new Predicate[0])));
    };
  }
}
