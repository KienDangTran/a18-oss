package com.a18.lottery.stream;

import org.springframework.cloud.stream.annotation.EnableBinding;

@EnableBinding({LotterySourceStream.class})
public class LotteryStreamConfig {
}
