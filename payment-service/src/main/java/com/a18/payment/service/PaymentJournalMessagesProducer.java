package com.a18.payment.service;

import com.a18.common.constant.Journal;
import com.a18.common.dto.JournalDTO;
import com.a18.common.util.AuthUtil;
import com.a18.payment.model.Tx;
import com.a18.payment.model.repository.TxRepository;
import com.a18.payment.stream.PaymentSourceStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import static com.a18.common.constant.RabbitMQExchange.PAYMENT_JOURNAL_EXCHANGE;

@Slf4j
@Component
@Lazy
public class PaymentJournalMessagesProducer {

  @Autowired @Lazy private AuthUtil authUtil;

  @Autowired @Lazy private TxRepository txRepository;

  @Autowired @Lazy private ObjectMapper objectMapper;

  @Autowired @Lazy private PaymentSourceStream paymentSourceStream;

  void sendPaymentJournal(Tx tx) {
    switch (tx.getJournal()) {
      case DEPOSIT:
        this.sendDepositJournal(tx);
        break;
      case WITHDRAWAL:
        this.sendWithdrawalJournal(tx);
        break;
    }
  }

  private void sendDepositJournal(Tx tx) {
    if (!Objects.equals(tx.getJournal(), Journal.DEPOSIT)
        || !Objects.equals(tx.getStatus(), Tx.TxStatus.SUCCESS)) { return; }

    if (!this.sendJournalMsg(tx, JournalDTO.JournalStatus.FINAL)) {
      tx.setStatus(Tx.TxStatus.IN_PROGRESS);
      this.txRepository.save(tx);
    }
  }

  private void sendWithdrawalJournal(Tx tx) {
    if (!Objects.equals(tx.getJournal(), Journal.WITHDRAWAL)) { return; }

    switch (tx.getStatus()) {
      case IN_PROGRESS:
        if (!this.sendJournalMsg(tx, JournalDTO.JournalStatus.IN_PROGRESS)) {
          tx.setStatus(Tx.TxStatus.FAILED);
          this.txRepository.save(tx);
        }
        break;
      case SUCCESS:
        if (!this.sendJournalMsg(tx, JournalDTO.JournalStatus.FINAL)) {
          tx.setStatus(Tx.TxStatus.IN_PROGRESS);
          this.txRepository.save(tx);
        }
        break;
      default:
        break;
    }
  }

  @Retryable
  private boolean sendJournalMsg(Tx tx, JournalDTO.JournalStatus status) {
    Assert.notNull(tx, "cannot sendJournalMsg 'coz tx is null");

    return this.paymentSourceStream.sendPaymentJournalMessage().send(
        MessageBuilder.withPayload(
            JournalDTO
                .builder()
                .amt(tx.getAmt())
                .ccy(tx.getPaymentChannel().getCcy())
                .gameCategory(tx.getGameCategory())
                .journal(tx.getJournal())
                .refId(tx.getId())
                .refType(Tx.class.getSimpleName().toUpperCase())
                .username(tx.getUsername())
                .amt(tx.getAmt())
                .registrationTokens(this.authUtil.getUserDeviceRegistrationToken())
                .status(status)
                .build()
        ).build(),
        30000
    );
  }

  @Transactional
  @RabbitListener(queues = "${spring.cloud.stream.rabbit.bindings."
      + PAYMENT_JOURNAL_EXCHANGE
      + ".producer.prefix}"
      + PAYMENT_JOURNAL_EXCHANGE
      + ".${spring.cloud.stream.default.producer.required-groups}.dlq")
  public void handleFailedJournalMsg(Message failedMessage) throws IOException {
    JournalDTO journal =
        objectMapper.readValue((byte[]) failedMessage.getPayload(), JournalDTO.class);
    log.error(
        "{} journal was failed: \n\theader={}; \n\tpayload={}",
        journal.getJournal(),
        failedMessage.getHeaders(),
        journal
    );

    Tx tx = this.txRepository.getOne(journal.getRefId());
    tx.setStatus(Tx.TxStatus.IN_PROGRESS);
    txRepository.save(tx);
  }
}
