package com.a18.auth.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;

@Data
@EqualsAndHashCode(of = {"username"})
@ToString(exclude = {"userDocuments"})
@Entity
@Table(name = "user", schema = "auth")
public class User extends AbstractUserDetails {
  @Column(nullable = false, unique = true, updatable = false)
  private String username;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private String firebaseUid;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private String referralLink;

  @OneToMany(mappedBy = "user", cascade = {CascadeType.MERGE, CascadeType.REFRESH})
  private Set<UserDocument> userDocuments = new HashSet<>();

  @JsonIgnore
  @Override public Collection<? extends GrantedAuthority> getAuthorities() {
    return Set.of();
  }
}
