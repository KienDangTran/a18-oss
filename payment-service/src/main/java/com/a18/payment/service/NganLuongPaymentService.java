package com.a18.payment.service;

import com.a18.common.exception.ApiException;
import com.a18.payment.model.Tx;
import com.a18.payment.model.dto.NganLuongResponse;
import com.a18.payment.model.dto.NganLuongResponse.NganLuongStatus;
import com.a18.payment.model.dto.TxDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Builder final class NganLuongPaymentService implements PaymentVendorService {
  private final TxDTO txDTO;

  private final Map<String, List<String>> vendorApiParams;

  private final String paymentMethodCode;

  private final String bankCode;

  private final String ccy;

  private final String depositURI;

  private final String withdrawalURI;

  private final String fullname;

  private final String email;

  private final String phone;

  private final String cardNo;

  private final String cardMonth;

  private final String cardYear;

  private final String bankAccount;

  private final String bankBranch;

  @Override
  public TxDTO sendDepositRequest() {
    Assert.notNull(txDTO, "cannot sendDepositRequest 'coz txDTO is null");
    Assert.isTrue(
        StringUtils.isNotBlank(paymentMethodCode),
        "cannot sendDepositRequest 'coz paymentMethodCode is blank"
    );
    Assert.isTrue(
        StringUtils.isNotBlank(bankCode),
        "cannot sendDepositRequest 'coz bankCode is blank"
    );
    Assert.isTrue(
        StringUtils.isNotBlank(fullname),
        "cannot sendDepositRequest 'coz buyer_fullname is blank"
    );
    Assert.isTrue(
        StringUtils.isNotBlank(email),
        "cannot sendDepositRequest 'coz buyer_email is blank"
    );
    Assert.isTrue(
        StringUtils.isNotBlank(phone),
        "cannot sendDepositRequest 'coz buyer_mobile is blank"
    );

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.put("function", List.of("SetExpressCheckout"));
    params.put("version", List.of("3.1"));
    params.put("payment_type", List.of("1"));
    params.put("order_code", List.of(txDTO.getCode()));
    params.put("total_amount", List.of(String.valueOf(txDTO.getAmt().toBigInteger())));
    params.put("payment_method", List.of(paymentMethodCode));
    params.put("bank_code", List.of(bankCode));
    params.put("buyer_fullname", List.of(fullname));
    params.put("buyer_email", List.of(email));
    params.put("buyer_mobile", List.of(phone));
    params.put("cur_code", List.of(ccy.toLowerCase()));
    params.put("lang_code", List.of(LocaleContextHolder.getLocale().getCountry()));

    return this.handleResponse(txDTO, postNganLuongRequest(depositURI, params));
  }

  @Override
  public TxDTO sendWithdrawRequest() {
    Assert.notNull(txDTO, "cannot sendWithdrawRequest 'coz txDTO is null");
    Assert.isTrue(
        StringUtils.isNotBlank(paymentMethodCode),
        "cannot sendWithdrawRequest 'coz paymentMethodCode is blank"
    );
    Assert.isTrue(
        StringUtils.isNotBlank(bankCode),
        "cannot sendWithdrawRequest 'coz bankCode is blank"
    );

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.put("func", List.of("SetCashoutRequest"));
    params.put("ref_code", List.of(this.txDTO.getCode()));
    params.put("total_amount", List.of(String.valueOf(txDTO.getAmt().toBigInteger())));
    params.put("bank_code", List.of(bankCode));
    params.put("card_fullname", List.of(fullname));
    params.put("reason", List.of(StringUtils.trimToEmpty(txDTO.getRemark())));

    switch (paymentMethodCode) {
      case "ATM_ONLINE":
        Assert.isTrue(
            StringUtils.isNotBlank(cardNo),
            "cannot sendWithdrawRequest 'coz cardNo is blank"
        );
        Assert.isTrue(
            !StringUtils.isAnyBlank(cardMonth, cardYear),
            "cannot sendWithdrawRequest 'coz cardExpireDate, cardIssueDate are all blank"
        );

        params.put("account_type", List.of("2"));

        params.put("card_month", List.of(String.valueOf(cardMonth)));
        params.put("card_year", List.of(String.valueOf(cardYear)));
        params.put("card_number", List.of(cardNo));
        break;
      case "IB_ONLINE":
        Assert.isTrue(
            StringUtils.isNotBlank(bankAccount),
            "cannot sendWithdrawRequest 'coz bankAccount is blank"
        );
        params.put("account_type", List.of("3"));
        params.put("card_number", List.of(StringUtils.trimToEmpty(bankAccount)));
        params.put("branch_name", List.of(StringUtils.trimToEmpty(bankBranch)));
        break;
      default:
        throw new ApiException("tx.unsupported.withdrawal.with.payment.method", paymentMethodCode);
    }

    return this.handleResponse(txDTO, postNganLuongRequest(withdrawalURI, params));
  }

  @Override
  public TxDTO getTransactionResult() {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    switch (txDTO.getJournal()) {
      case DEPOSIT:
        if (StringUtils.isBlank(txDTO.getToken())) {
          throw new ApiException("tx.token.is.missing", txDTO.getCode());
        }
        params.put("function", List.of("GetTransactionDetail"));
        params.put("token", List.of(txDTO.getToken()));
        return this.handleResponse(txDTO, this.postNganLuongRequest(depositURI, params));
      case WITHDRAWAL:
        params.put("func", List.of("CheckCashout"));
        params.put("transaction_id", List.of(StringUtils.trimToEmpty(txDTO.getInvoiceNo())));
        params.put("ref_code", List.of(txDTO.getCode()));
        return this.handleResponse(txDTO, this.postNganLuongRequest(withdrawalURI, params));
      default:
        throw new UnsupportedOperationException("tx.journal.not.supported"
            + ": "
            + txDTO.getJournal().name());
    }
  }

  private NganLuongResponse postNganLuongRequest(String uri, MultiValueMap<String, String> params) {
    Assert.notNull(uri, "cannot postNganLuongRequest 'coz uri is null");
    Assert.notEmpty(params, "cannot postNganLuongRequest 'coz params is empty");

    params.putAll(vendorApiParams);
    RestTemplate restTemplate = new RestTemplate();
    RequestEntity request = RequestEntity
        .post(URI.create(uri))
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .header(HttpHeaders.CACHE_CONTROL, "no-cache")
        .body(params);

    ResponseEntity result = restTemplate.exchange(request, String.class);

    if (!Objects.equals(HttpStatus.OK, result.getStatusCode()) || result.getBody() == null) {
      throw new ApiException("tx.cannot.process.tx", txDTO.getCode(), uri);
    }

    return this.readResponse(String.valueOf(result.getBody()));
  }

  private NganLuongResponse readResponse(String result) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      return objectMapper.readValue(result, NganLuongResponse.class);
    } catch (IOException e) {
      try (StringReader reader = new StringReader(result)) {
        JAXBContext jContext = JAXBContext.newInstance(NganLuongResponse.class);
        Unmarshaller unmarshaller = jContext.createUnmarshaller();
        return (NganLuongResponse) unmarshaller.unmarshal(reader);
      } catch (JAXBException ex) {
        ex.printStackTrace();
        throw new ApiException("tx.cannot.read.ngan.luong.response", result);
      }
    }
  }

  private TxDTO handleResponse(TxDTO txDTO, NganLuongResponse response) {
    switch (response.getError_code()) {
      case E_00:
        return this.handleSuccess(txDTO, response);
      case E_04:
      case E_81:
      case E_99:
        return this.handlePending(txDTO, response);
      default:
        return this.handleFailed(txDTO, response);
    }
  }

  private TxDTO handlePending(TxDTO txDTO, NganLuongResponse response) {
    return txDTO
        .withStatus(this.mapNLStatusToTxStatus(
            response.getTransaction_status(),
            Tx.TxStatus.IN_PROGRESS
        ))
        .withRemark(StringUtils.joinWith(
            "; ",
            StringUtils.trimToEmpty(response.getError_code().getDesc()),
            StringUtils.trimToEmpty(response.getDescription())
        ));
  }

  private TxDTO handleSuccess(TxDTO txDTO, NganLuongResponse response) {
    return txDTO
        .withCheckoutUrl(Objects.toString(response.getCheckout_url(), txDTO.getCheckoutUrl()))
        .withToken(Objects.toString(response.getToken(), txDTO.getToken()))
        .withErrorCode(response.getError_code().getErrCode())
        .withInvoiceNo(response.getTransaction_id())
        .withStatus(this.mapNLStatusToTxStatus(
            response.getTransaction_status(),
            Tx.TxStatus.IN_PROGRESS
        ))
        .withAmt(new BigDecimal(NumberUtils.toDouble(
            response.getTotal_amount(),
            txDTO.getAmt().doubleValue()
        )))
        .withRemark(StringUtils.joinWith(
            "; ",
            Objects.isNull(response.getTransaction_status())
            ? ""
            : response.getTransaction_status().getDesc(),
            StringUtils.trimToEmpty(response.getDescription())
        ));
  }

  private TxDTO handleFailed(TxDTO txDTO, NganLuongResponse response) {
    return txDTO
        .withErrorCode(response.getError_code().getErrCode())
        .withStatus(this.mapNLStatusToTxStatus(
            response.getTransaction_status(),
            Tx.TxStatus.IN_PROGRESS
        ))
        .withRemark(StringUtils.joinWith(
            "; ",
            StringUtils.trimToEmpty(response.getError_code().getDesc()),
            StringUtils.trimToEmpty(response.getDescription())
        ));
  }

  private Tx.TxStatus mapNLStatusToTxStatus(
      NganLuongStatus status,
      @Nullable Tx.TxStatus defaultTxStatus
  ) {
    if (Objects.isNull(status)) return defaultTxStatus;

    switch (status) {
      case S_00:
        return Tx.TxStatus.SUCCESS;
      case S_01:
      case S_02:
        return Tx.TxStatus.IN_PROGRESS;
      default:
        return defaultTxStatus;
    }
  }
}
