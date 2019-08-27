package com.a18.payment.service;

import com.a18.common.dto.UserDTO;
import com.a18.common.exception.ApiException;
import com.a18.common.util.AuthUtil;
import com.a18.payment.model.PaymentCard;
import com.a18.payment.model.PaymentChannel;
import com.a18.payment.model.PaymentVendor;
import com.a18.payment.model.Tx;
import com.a18.payment.model.dto.TxDTO;
import com.a18.payment.model.repository.PaymentCardRepository;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Slf4j
@Component
@Lazy
class PaymentFactory {

  @Autowired @Lazy private AuthUtil authUtil;

  @Autowired @Lazy private PaymentCardRepository paymentCardRepository;

  private PaymentVendorService getVendorService(TxDTO txDTO) {
    Assert.notNull(txDTO, "cannot getVendorService 'coz txDTO is null");
    Assert.notNull(
        txDTO.getPaymentChannel(),
        "cannot getVendorService 'coz payment channel is null"
    );

    UserDTO userDTO = this.authUtil
        .retrieveUserInfoByUsername(txDTO.getUsername())
        .orElseThrow(() -> new ApiException("user.info.cannot.retrieve"));

    switch (txDTO.getPaymentChannel().getPaymentVendor().getCode()) {
      case PaymentVendor.NGAN_LUONG:
        return createNganLuongService(txDTO.withUsername(userDTO.getUsername()), userDTO);
      case PaymentVendor.HELP_2_PAY:
        throw new UnsupportedOperationException("Unsupported Payment Vendor: "
            + txDTO.getPaymentChannel().getPaymentVendor().getCode());
      default:
        throw new UnsupportedOperationException("Unsupported Payment Vendor: "
            + txDTO.getPaymentChannel().getPaymentVendor().getCode());
    }
  }

  TxDTO createDepositRequest(TxDTO txDTO) {
    Assert.notNull(txDTO, "cannot createDepositRequest 'coz txDTO is null");

    return getVendorService(txDTO).sendDepositRequest();
  }

  TxDTO createWithdrawRequest(TxDTO txDTO) {
    Assert.notNull(txDTO, "cannot createWithdrawRequest 'coz txDTO is null");

    if (!txDTO.getPaymentChannel().getAutoApprove()
        && Objects.equals(Tx.TxStatus.NEW, txDTO.getStatus())) {
      return txDTO.withStatus(Tx.TxStatus.PENDING);
    }

    return getVendorService(txDTO).sendWithdrawRequest();
  }

  TxDTO getAndProcessTxResult(TxDTO txDTO) {
    Assert.notNull(txDTO, "cannot getAndProcessTxResult 'coz txDTO is null");
    return getVendorService(txDTO).getTransactionResult();
  }

  TxDTO acceptWithdrawalTx(TxDTO txDTO) {
    return getVendorService(txDTO).sendWithdrawRequest();
  }

  private NganLuongPaymentService createNganLuongService(TxDTO txDTO, UserDTO userDTO) {
    PaymentChannel channel = txDTO.getPaymentChannel();
    switch (txDTO.getJournal()) {
      case DEPOSIT:
        return NganLuongPaymentService
            .builder()
            .txDTO(txDTO)
            .vendorApiParams(this.getMerchantInfo(channel.getPaymentVendor()))
            .paymentMethodCode(channel.getPaymentMethod().getCode())
            .bankCode(channel.getBank().getCode())
            .ccy(channel.getCcy().name())
            .depositURI(channel.getPaymentVendor().getDepositUri())
            .fullname(userDTO.getFullname())
            .email(userDTO.getEmail())
            .phone(userDTO.getPhone())
            .build();
      case WITHDRAWAL:
        PaymentCard card = this.getUserPaymentCard(channel, userDTO.getUsername());
        return NganLuongPaymentService
            .builder()
            .txDTO(txDTO)
            .vendorApiParams(this.getMerchantInfo(channel.getPaymentVendor()))
            .paymentMethodCode(channel.getPaymentMethod().getCode())
            .bankCode(channel.getBank().getCode())
            .withdrawalURI(channel.getPaymentVendor().getWithdrawalUri())
            .fullname(userDTO.getFullname())
            .cardNo(card.getCardNo())
            .cardMonth(Objects.toString(card.getCardMonth(), ""))
            .cardYear(Objects.toString(card.getCardYear(), ""))
            .bankAccount(card.getBankAccount())
            .bankBranch(card.getBranch())
            .build();
      default:
        throw new UnsupportedOperationException("tx.journal.not.supported"
            + ": "
            + txDTO.getJournal().name());
    }
  }

  private PaymentCard getUserPaymentCard(PaymentChannel channel, String username) {
    if (channel.getRequiredCardType() != null) {
      return this.paymentCardRepository
          .findByUsernameAndBankIdAndTypeAndCcyAndStatusIn(
              username,
              channel.getBank().getId(),
              channel.getRequiredCardType(),
              channel.getCcy(),
              Set.of(PaymentCard.PaymentCardStatus.ACTIVE)
          )
          .orElseThrow(() -> new ApiException("user.card.cannot.retrieve"));
    } else if (channel.getBankAccountRequired()) {
      return this.paymentCardRepository
          .findAllByUsernameAndBankIdAndBankAccountNotNullAndCcyAndStatusIn(
              username,
              channel.getBank().getId(),
              channel.getCcy(),
              Set.of(PaymentCard.PaymentCardStatus.ACTIVE)
          )
          .stream()
          .findAny()
          .orElseThrow(() -> new ApiException("user.card.cannot.retrieve"));
    } else {
      throw new ApiException(
          "cannot specify either card type /bank account is required in payment channel",
          channel.getId()
      );
    }
  }

  private Map<String, List<String>> getMerchantInfo(PaymentVendor vendor) {
    return vendor
        .getVendorApiParams()
        .stream()
        .filter(vendorApiParam -> StringUtils.isNotBlank(vendorApiParam.getDefaultValue()))
        .map(vendorApiParam -> new AbstractMap.SimpleEntry<>(
            vendorApiParam.getKey(),
            List.of(vendorApiParam.getDefaultValue())
        ))
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue
        ));
  }
}
