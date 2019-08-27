package com.a18.common.firebase;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Lazy
public class FCMUtils {

  @Value("${firebase.dry-run:false}")
  private boolean dryRunMode;

  @Autowired @Lazy FirebaseConfig firebaseConfig;

  @Retryable
  @Async
  public void pushNotification(
      String title,
      String body,
      Object additionalData,
      String registrationToken
  ) {
    if (StringUtils.isBlank(registrationToken)) {
      log.warn("registrationToken is blank");
      return;
    }

    try {
      log.trace(
          "FCM sending: {notification: {title: {}, body: {}}, data: {}, registrationToken: {}",
          title,
          body,
          additionalData,
          registrationToken
      );
      this.firebaseConfig.getFirebaseMessagingInstance().send(
          Message.builder()
                 .setToken(registrationToken)
                 .setNotification(new Notification(title, body))
                 .putAllData(this.firebaseConfig
                     .objectToMap(additionalData)
                     .entrySet()
                     .parallelStream()
                     .collect(Collectors.toMap(
                         Map.Entry::getKey,
                         e -> Objects.toString(e.getValue(), null)
                     ))
                 )
                 .build(),
          this.dryRunMode
      );
    } catch (FirebaseMessagingException e) {
      log.trace("Cannot push notification to FCM", e);
    }
  }
}
