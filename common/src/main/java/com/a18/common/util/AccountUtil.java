package com.a18.common.util;

import com.a18.common.constant.Ccy;
import com.a18.common.constant.GameCategory;
import com.a18.common.dto.BalanceDTO;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;

@Component
@Lazy
@Slf4j
public class AccountUtil {
  public static final String PATH_SEARCH_BALANCE =
      "/balances/findBalanceByUsernameAndCcyAndGameCategory";

  @Value("${account-service-uri}")
  private String accountServiceUrl;

  @Autowired @Lazy private OAuth2RestTemplate restTemplate;

  public Optional<BalanceDTO> findBalanceByUsernameAndCcyAndGameCategory(
      String username,
      Ccy ccy,
      GameCategory gameCategory
  ) throws HttpClientErrorException {
    Assert.notNull(
        username,
        "cannot findBalanceByUsernameAndCcyAndGameCategory coz username is null"
    );
    Assert.notNull(ccy, "cannot findBalanceByUsernameAndCcyAndGameCategory coz ccy is null");
    Assert.notNull(
        gameCategory,
        "cannot findBalanceByUsernameAndCcyAndGameCategory coz gameCategory is null"
    );

    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.put("username", List.of(username));
    params.put("ccy", List.of(ccy.name()));
    params.put("gameCategory", List.of(gameCategory.name()));
    RequestEntity request = RequestEntity
        .post(URI.create(this.accountServiceUrl + PATH_SEARCH_BALANCE))
        .header(HttpHeaders.CACHE_CONTROL, "no-cache")
        .body(params);

    return Optional.ofNullable(this.restTemplate.exchange(request, BalanceDTO.class).getBody());
  }
}
