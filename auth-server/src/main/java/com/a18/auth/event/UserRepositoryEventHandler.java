package com.a18.auth.event;

import com.a18.auth.model.User;
import com.a18.common.constant.Privilege;
import com.a18.common.dto.UserDTO;
import com.a18.common.firebase.FirebaseAuthUtils;
import com.google.firebase.auth.FirebaseAuthException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Lazy
@RepositoryEventHandler
public class UserRepositoryEventHandler {
  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired @Lazy private FirebaseAuthUtils firebaseAuthUtils;

  @HandleBeforeCreate
  public void handleBeforeCreateUser(User user) {
    user.setPassword(StringUtils.trimToNull(this.passwordEncoder.encode(user.getPassword())));
    this.setMappingEntity(user);

    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
        user.getUsername(),
        null,
        List.of(new SimpleGrantedAuthority(Privilege.write(Privilege.USER)))
    ));

    try {
      this.firebaseAuthUtils.getUserByPhone(user.getPhone()).get().ifPresentOrElse(
          userDTO -> user.setFirebaseUid(userDTO.getFirebaseUid()),
          () -> user.setEnabled(false)
      );
    } catch (InterruptedException | ExecutionException | FirebaseAuthException e) {
      user.setEnabled(false);
    }
  }

  @HandleAfterCreate
  public void handleAfterCreate(User user) {
    SecurityContextHolder.clearContext();
    saveFirebaseUser(user);
  }

  @HandleBeforeSave
  public void handleBeforeSaveUser(User user) {
    this.setMappingEntity(user);
  }

  @HandleAfterSave
  public void handleAfterSaveUser(User user) {
    saveFirebaseUser(user);
  }

  private void saveFirebaseUser(User user) {
    try {
      this.firebaseAuthUtils.saveOrUpdateUserRecord(this.createUserDTO(user));
    } catch (ExecutionException | InterruptedException | FirebaseAuthException e) {
      log.error("sending message to FCM is failed: {}", e.getCause().getMessage());
      log.debug("sending message to FCM is failed: {}", e);
    }
  }

  private UserDTO createUserDTO(User user) {
    return UserDTO.builder()
                  .id(user.getId())
                  .username(user.getUsername())
                  .fullname(user.getFullname())
                  .email(user.getEmail())
                  .phone(user.getPhone())
                  .enabled(user.isEnabled())
                  .build();
  }

  private void setMappingEntity(User user) {
    if (!user.getUserDocuments().isEmpty()) {
      user.getUserDocuments().forEach(doc -> doc.setUser(user));
    }
  }
}
