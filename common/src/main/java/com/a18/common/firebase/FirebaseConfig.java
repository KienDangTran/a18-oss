package com.a18.common.firebase;

import com.a18.common.exception.ApiException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Slf4j
@Lazy
@Component
public class FirebaseConfig {

  @Value("${firebase.project-name}")
  String firebaseAppName;

  @Value("${firebase.service-account.credential.file}")
  private Resource firebaseCredential;

  @Value("${firebase.web-api-key}")
  public String firebaseWebApiKey;

  @Value("${firebase.db-url}")
  private String firebaseDbUrl;

  @Autowired @Lazy private ObjectMapper objectMapper;

  @PostConstruct
  public void initFirebaseApp() throws IOException {
    if (FirebaseApp.getApps()
                   .stream()
                   .noneMatch(app -> this.firebaseAppName.equalsIgnoreCase(app.getName()))) {
      FirebaseOptions options = new FirebaseOptions.Builder()
          .setCredentials(GoogleCredentials.fromStream(this.firebaseCredential.getInputStream()))
          .setDatabaseUrl(this.firebaseDbUrl)
          .build();

      FirebaseApp app = FirebaseApp.initializeApp(options, this.firebaseAppName);
      FirebaseInstanceId.getInstance(app);
    }
  }

  public FirebaseCredentials getFirebaseCredentials() {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readValue(this.firebaseCredential.getInputStream(), FirebaseCredentials.class);
    } catch (IOException e) {
      log.error(e.getMessage(), e);
      throw new ApiException(e.getMessage());
    }
  }

  private FirebaseApp getFirebaseAppInstance() {
    return FirebaseApp.getInstance(this.firebaseAppName);
  }

  FirebaseAuth getFirebaseAuthInstance() {
    return FirebaseAuth.getInstance(this.getFirebaseAppInstance());
  }

  FirebaseMessaging getFirebaseMessagingInstance() {
    return FirebaseMessaging.getInstance(this.getFirebaseAppInstance());
  }

  Firestore getFirestoreInstance() {
    return FirestoreClient.getFirestore(this.getFirebaseAppInstance());
  }

  Map<String, ?> objectToMap(Object o) {
    if (o == null) return Map.of();

    Map<String, ?> map = objectMapper.convertValue(o, new TypeReference<Map<String, ?>>() {});
    return map.entrySet()
              .stream()
              .map(this::entryToString)
              .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private Map.Entry<String, ?> entryToString(Map.Entry<String, ?> entry) {
    if (Map.class.isAssignableFrom(entry.getValue().getClass())) {
      return new AbstractMap.SimpleImmutableEntry<>(
          entry.getKey(),
          this.objectToMap(entry.getValue())
      );
    }

    if (Collection.class.isAssignableFrom(entry.getValue().getClass())) {
      List<Map<String, ?>> values =
          List.copyOf((Collection<?>) entry.getValue())
              .stream()
              .map(this::objectToMap)
              .collect(Collectors.toUnmodifiableList());

      return new AbstractMap.SimpleImmutableEntry<>(entry.getKey(), values);
    }

    return new AbstractMap.SimpleImmutableEntry<>(
        entry.getKey(),
        entry.getValue().toString()
    );
  }
}
