package com.a18.account;

import java.util.Locale;
import java.util.TimeZone;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan({"com.a18.account", "com.a18.common"})
@SpringBootApplication
public class AccountServiceApplication {

  @Value("${timezone:UTC}")
  private String timezone;

  @Value("${locale:vi-VN}")
  private String locale;

  public static void main(String[] args) {
    SpringApplication.run(AccountServiceApplication.class, args);
  }

  @PostConstruct
  public void init() {
    TimeZone.setDefault(TimeZone.getTimeZone(timezone));
    Locale.setDefault(Locale.forLanguageTag(locale));
  }
}
