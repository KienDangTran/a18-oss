package com.a18.payment.stream;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

import static com.a18.common.constant.RabbitMQExchange.PAYMENT_JOURNAL_EXCHANGE;

public interface PaymentSourceStream {

  @Output(PAYMENT_JOURNAL_EXCHANGE)
  MessageChannel sendPaymentJournalMessage();
}
