package com.a18.account.stream;

import org.springframework.cloud.stream.annotation.EnableBinding;

@EnableBinding({PaymentSinkStream.class, LotterySinkStream.class})
public class AccountStreamConfig {
}
