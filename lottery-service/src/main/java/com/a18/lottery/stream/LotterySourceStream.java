package com.a18.lottery.stream;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

import static com.a18.common.constant.RabbitMQExchange.LOTTERY_JOURNAL_EXCHANGE;

public interface LotterySourceStream {
  @Output(LOTTERY_JOURNAL_EXCHANGE) MessageChannel sendLotteryJournalMessage();
}
