package com.a18.auth.model;

import com.a18.common.dto.BaseEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnTransformer;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
@ToString(exclude = {"password"})
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@MappedSuperclass
public abstract class AbstractUserDetails extends BaseEntity implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Column(nullable = false, updatable = false)
  private String password;

  @Column(nullable = false)
  @ColumnTransformer(read = "UPPER(fullname)", write = "UPPER(?)")
  private String fullname;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false, unique = true)
  private String phone;

  private LocalDate dateOfBirth;

  private Boolean enabled = true;

  private Boolean accountNonLocked = true;

  private Boolean accountNonExpired = true;

  private Boolean credentialsNonExpired = true;

  @Override public boolean isAccountNonExpired() {
    return this.accountNonExpired;
  }

  @Override public boolean isAccountNonLocked() {
    return this.accountNonLocked;
  }

  @Override public boolean isCredentialsNonExpired() {
    return this.credentialsNonExpired;
  }

  @Override public boolean isEnabled() {
    return this.enabled;
  }
}
