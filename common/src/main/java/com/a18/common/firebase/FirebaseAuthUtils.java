package com.a18.common.firebase;

import com.a18.common.dto.UserDTO;
import com.google.api.core.ApiFuture;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
@Lazy
public class FirebaseAuthUtils {

  @Autowired @Lazy FirebaseConfig firebaseConfig;

  @Retryable
  @Async
  public Future<Optional<UserDTO>> getUserByPhone(String phone)
      throws FirebaseAuthException {
    Assert.notNull(phone, "cannot getUserByPhone 'coz phone is null");

    UserRecord record =
        this.firebaseConfig.getFirebaseAuthInstance().getUserByPhoneNumber(phone);
    return CompletableFuture.completedFuture(Optional.ofNullable(
        UserDTO.builder()
               .username(record.getDisplayName())
               .firebaseUid(record.getUid())
               .email(record.getEmail())
               .emailVerified(record.isEmailVerified())
               .phone(record.getPhoneNumber())
               .enabled(!record.isDisabled())
               .build()
    ));
  }

  @Async
  @Retryable
  public void saveOrUpdateUserRecord(UserDTO userDTO)
      throws ExecutionException, InterruptedException, FirebaseAuthException {
    Assert.notNull(userDTO, "cannot saveOrUpdateUserRecord 'coz userDTO is null");

    Optional<UserDTO> newUserDTO = this.getUserByPhone(userDTO.getPhone()).get();
    if (newUserDTO.isPresent()) {
      this.updateUserRecord(
          newUserDTO.get().getFirebaseUid(),
          userDTO.getUsername(),
          userDTO.getPhone(),
          userDTO.getEmail(),
          !newUserDTO.get().isEnabled()
      );
    } else {
      this.createUserRecord(userDTO);
    }
  }

  private ApiFuture<UserRecord> createUserRecord(UserDTO userDTO) {
    return this.firebaseConfig.getFirebaseAuthInstance().createUserAsync(
        new UserRecord.CreateRequest()
            .setUid(String.valueOf(userDTO.getId()))
            .setDisplayName(userDTO.getUsername())
            .setPhoneNumber(userDTO.getPhone())
            .setEmail(userDTO.getEmail())
            .setEmailVerified(false)
            .setDisabled(!userDTO.isEnabled())
    );
  }

  private ApiFuture<UserRecord> updateUserRecord(
      String uid,
      String username,
      String phone,
      String email,
      boolean disabled
  ) {
    return this.firebaseConfig.getFirebaseAuthInstance().updateUserAsync(
        new UserRecord.UpdateRequest(uid)
            .setDisplayName(username)
            .setPhoneNumber(phone)
            .setEmail(email)
            .setDisabled(disabled)
    );
  }
}
