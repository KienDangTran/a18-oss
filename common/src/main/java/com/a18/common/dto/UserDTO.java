package com.a18.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
  private Long id;

  private String username;

  private String fullname;

  private String firebaseUid;

  private String email;

  private boolean emailVerified;

  private String phone;

  private boolean enabled;
}
