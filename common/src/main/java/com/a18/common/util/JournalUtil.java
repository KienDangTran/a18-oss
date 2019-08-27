package com.a18.common.util;

import com.a18.common.constant.Journal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

@UtilityClass
public class JournalUtil {
  public String genJournalCode(Journal journal, String username) {
    Assert.isTrue(
        journal != null && StringUtils.isNotBlank(username),
        "cannot generate code for null journal and/or blank username"
    );
    return new StringBuilder(username)
        .append("$")
        .append(journal.name())
        .append("$")
        .append(LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond())
        .toString();
  }
}
