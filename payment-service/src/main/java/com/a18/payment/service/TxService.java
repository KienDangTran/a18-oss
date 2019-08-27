package com.a18.payment.service;

import com.a18.common.constant.Journal;
import com.a18.common.dto.JournalDTO;
import com.a18.common.exception.ApiException;
import com.a18.common.firebase.FirestoreUtils;
import com.a18.payment.model.PaymentChannel;
import com.a18.payment.model.Tx;
import com.a18.payment.model.Tx.TxStatus;
import com.a18.payment.model.dto.TxDTO;
import com.a18.payment.model.repository.PaymentChannelRepository;
import com.a18.payment.model.repository.TxRepository;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

@Service
public class TxService {

  @Autowired @Lazy private TxRepository txRepository;

  @Autowired @Lazy private PaymentFactory paymentFactory;

  @Autowired @Lazy private PaymentJournalMessagesProducer paymentJournalMessagesProducer;

  @Autowired @Lazy private FirestoreUtils firestoreUtils;

  @Autowired @Lazy private PaymentChannelRepository channelRepository;

  /**
   * <pre>
   * - send a createDepositRequest/withdrawal request to payment vendor
   * - if vendor accepts the request with success response, create a new {@link Tx} entity and save
   *   to DB
   * - createDepositRequest tx will be put into {@link TxStatus#IN_PROGRESS} status
   * - for withdrawal tx:
   *  + if {@link PaymentChannel#getAutoApprove()} = true, request will be sent to vendor and processed
   *    immediately, if tx is succeed, a {@link JournalDTO} message will be published
   *  + else tx will be put into {@link TxStatus#PENDING} status and need to be sent manually by
   *    calling {@link #approveTx} function
   * - failed tx will not be stored
   *
   * </pre>
   */
  @Transactional
  public Tx createTxRequest(TxDTO txDTO) {
    Assert.notNull(txDTO, "cannot createTxRequest 'coz txDTO is null");
    PaymentChannel channel = this.channelRepository.getOne(txDTO.getPaymentChannelId());
    Tx tx;
    switch (txDTO.getJournal()) {
      case DEPOSIT:
        tx = new Tx(this.paymentFactory.createDepositRequest(txDTO.withPaymentChannel(channel)));
        break;
      case WITHDRAWAL:
        tx = new Tx(this.paymentFactory.createWithdrawRequest(txDTO.withPaymentChannel(channel)));
        break;
      default:
        throw new UnsupportedOperationException("tx.journal.not.supported"
            + ": "
            + txDTO.getJournal().name());
    }

    if (Objects.equals(TxStatus.FAILED, tx.getStatus())) {
      return tx;
    } else {
      tx = this.txRepository.save(tx);
      if (Objects.equals(TxStatus.PENDING, tx.getStatus())) {
        this.firestoreUtils.addData(
            tx,
            "payment",
            "tx",
            Journal.WITHDRAWAL.name().toLowerCase(),
            tx.getId().toString()
        );
      } else {
        this.paymentJournalMessagesProducer.sendPaymentJournal(tx);
      }

      return tx;
    }
  }

  /**
   * <pre>
   *   - Retrieving tx's result from vendor
   * </pre>
   */
  @Transactional
  public Tx getAndProcessTxResult(Long id) {
    Tx tx = this.txRepository.getOne(id);

    if (!Objects.equals(tx.getStatus(), TxStatus.IN_PROGRESS)) {
      return tx;
    }

    PaymentChannel channel = this.channelRepository.getOne(tx.getPaymentChannelId());

    this.updateExistingTxFromTxDTO(
        tx,
        this.paymentFactory.getAndProcessTxResult(new TxDTO(tx).withPaymentChannel(channel))
    );

    this.paymentJournalMessagesProducer.sendPaymentJournal(tx);

    return tx;
  }

  @Transactional
  public Tx approveTx(TxDTO txDTO) {
    Tx tx = this.txRepository.getOne(txDTO.getId());

    if (!Objects.equals(TxStatus.PENDING, tx.getStatus())) {
      return tx;
    }

    if (Objects.equals(txDTO.getStatus(), TxStatus.REJECTED)) {
      this.updateExistingTxFromTxDTO(tx, txDTO);
    } else {
      PaymentChannel channel = this.channelRepository.getOne(tx.getPaymentChannelId());
      this.updateExistingTxFromTxDTO(
          tx,
          this.paymentFactory.acceptWithdrawalTx(new TxDTO(tx).withPaymentChannel(channel))
      );
      this.paymentJournalMessagesProducer.sendPaymentJournal(tx);
    }

    this.firestoreUtils.deleteDocument(
        "payment",
        "tx",
        Journal.WITHDRAWAL.name().toLowerCase(),
        tx.getId().toString()
    );

    return tx;
  }

  private void updateExistingTxFromTxDTO(Tx existingTx, TxDTO txDTO) {
    Assert.notNull(existingTx, "cannot updateExistingTxFromTxDTO 'coz existingTx is null");
    Assert.notNull(txDTO, "cannot updateExistingTxFromTxDTO 'coz txDTO is null");

    if (!Objects.isNull(txDTO.getAmt())) existingTx.setAmt(txDTO.getAmt());
    if (!Objects.isNull(txDTO.getStatus())) existingTx.setStatus(txDTO.getStatus());
    if (StringUtils.isNotBlank(txDTO.getRemark())) existingTx.setRemark(txDTO.getRemark());
    if (StringUtils.isNotBlank(txDTO.getErrorCode())) existingTx.setErrorCode(txDTO.getErrorCode());
    if (StringUtils.isNotBlank(txDTO.getInvoiceNo())) existingTx.setInvoiceNo(txDTO.getInvoiceNo());
    if (StringUtils.isNotBlank(txDTO.getToken())) existingTx.setToken(txDTO.getToken());
    this.txRepository.save(existingTx);
  }

  public void deleteTx(Long id) {
    Tx tx = this.txRepository.getOne(id);
    if (CollectionUtils.containsInstance(Set.of(TxStatus.NEW, TxStatus.PENDING), tx.getStatus())) {
      this.txRepository.delete(tx);
    } else {
      throw new ApiException("tx.only.delete.new.or.pending", id);
    }
  }

  public Page<Tx> findBySpec(Specification<Tx> spec, Pageable pageable) {
    return this.txRepository.findAll(spec, pageable);
  }
}