package com.a18.payment.service;

import com.a18.common.firebase.FirestoreUtils;
import com.a18.payment.model.repository.BankRepository;
import com.a18.payment.model.repository.PaymentChannelRepository;
import com.a18.payment.model.repository.PaymentMethodRepository;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class PaymentMetadata {

  @Autowired @Lazy FirestoreUtils firestoreUtils;

  @Autowired @Lazy PaymentChannelRepository paymentChannelRepository;

  @Autowired @Lazy PaymentMethodRepository paymentMethodRepository;

  @Autowired @Lazy BankRepository bankRepository;

  @PostConstruct
  public void initPaymentMetadataInFireStore() {

    paymentMethodRepository
        .findAll()
        .forEach(paymentMethod -> this.firestoreUtils.addData(
            paymentMethod,
            "payment",
            "metadata",
            "payment_method",
            paymentMethod.getId().toString()
        ));

    bankRepository
        .findAll()
        .forEach(bank -> this.firestoreUtils.addData(
            bank,
            "payment",
            "metadata",
            "bank",
            bank.getId().toString()
        ));

    paymentChannelRepository
        .findAll()
        .forEach(paymentChannel -> this.firestoreUtils.addData(
            paymentChannel,
            "payment",
            "metadata",
            "payment_channel",
            paymentChannel.getId().toString()
        ));
  }
}
