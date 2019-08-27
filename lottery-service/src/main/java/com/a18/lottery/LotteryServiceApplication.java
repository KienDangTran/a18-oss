package com.a18.lottery;

import com.a18.lottery.scheduler.QuickLotteryScheduler;
import com.a18.lottery.scheduler.TraditionalLotteryScheduler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Locale;
import java.util.TimeZone;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Lazy;

@ComponentScan({"com.a18.lottery", "com.a18.common"})
@SpringBootApplication
public class LotteryServiceApplication {

  @Value("${timezone:UTC}")
  private String timezone;

  @Value("${locale:vi-VN}")
  private String locale;

  @Autowired @Lazy TraditionalLotteryScheduler traditionalLotteryScheduler;

  @Autowired @Lazy QuickLotteryScheduler quickLotteryScheduler;

  @Autowired @Lazy ObjectMapper objectMapper;

  public static void main(String[] args) {
    SpringApplication.run(LotteryServiceApplication.class, args);
  }

  @PostConstruct
  public void init() {
    TimeZone.setDefault(TimeZone.getTimeZone(timezone));
    Locale.setDefault(Locale.forLanguageTag(locale));
    this.traditionalLotteryScheduler.init();
    this.quickLotteryScheduler.init();
  }
}
