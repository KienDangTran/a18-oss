package com.a18.payment.stream;

import org.springframework.cloud.stream.annotation.EnableBinding;

@EnableBinding({PaymentSourceStream.class})
public class PaymentStreamConfig {
}
