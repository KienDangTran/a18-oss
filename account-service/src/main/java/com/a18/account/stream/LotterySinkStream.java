package com.a18.account.stream;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

import static com.a18.common.constant.RabbitMQExchange.LOTTERY_JOURNAL_EXCHANGE;

public interface LotterySinkStream {
  @Input(LOTTERY_JOURNAL_EXCHANGE) SubscribableChannel listenLotteryJournalExchange();
}
