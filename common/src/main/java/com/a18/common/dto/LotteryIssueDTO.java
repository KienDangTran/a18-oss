package com.a18.common.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Getter
public class LotteryIssueDTO implements Serializable {
  public String id;

  public String code;

  public String openingTime;

  public String closingTime;

  public String endingTime;

  public String status;

  public String results;
}
