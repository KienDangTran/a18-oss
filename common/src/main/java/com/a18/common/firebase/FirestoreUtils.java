package com.a18.common.firebase;

import com.google.cloud.firestore.Firestore;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Slf4j
@Component
@Lazy
public class FirestoreUtils {

  @Autowired @Lazy FirebaseConfig firebaseConfig;

  @Async
  @Retryable
  public void addData(Object data, String... documentPathParts) {
    Assert.isTrue(
        documentPathParts != null && documentPathParts.length > 0,
        "firestore collection & document's path cannot be blank"
    );
    Assert.isTrue(data != null, "data cannot be null");

    Map<String, ?> map = this.firebaseConfig.objectToMap(data);
    if (map.isEmpty()) return;

    Firestore firestore = firebaseConfig.getFirestoreInstance();
    String path = StringUtils.join(documentPathParts, "/");
    if (documentPathParts.length % 2 == 0) {
      firestore.document(path).set(map);
    } else {
      firestore.collection(path).add(map);
    }
  }

  @Async
  public void deleteDocument(String... documentPathParts) {
    Assert.isTrue(
        documentPathParts != null && documentPathParts.length > 0,
        "firestore collection & document's path cannot be blank"
    );

    String path = StringUtils.join(documentPathParts, "/");
    Firestore firestore = firebaseConfig.getFirestoreInstance();
    if (documentPathParts.length % 2 == 0) {
      firestore.document(path).delete();
    } else {
      firestore.collection(path).document().delete();
    }
  }
}
