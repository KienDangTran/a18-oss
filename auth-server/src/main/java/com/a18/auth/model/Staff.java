package com.a18.auth.model;

import com.a18.common.constant.Privilege;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Data
@EqualsAndHashCode(of = {"username", "staffRoles"})
@ToString(exclude = "staffRoles")
@Entity
@Table(name = "staff", schema = "auth")
public class Staff extends AbstractUserDetails {
  @Column(nullable = false, unique = true, updatable = false)
  private String username;

  @OneToMany(mappedBy = "staff", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<StaffRole> staffRoles = new HashSet<>();

  @JsonIgnore
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return this.staffRoles
        .stream()
        .filter(staffRole -> Role.RoleStatus.ACTIVE.equals(staffRole.getRole().getStatus())
            && !staffRole.getRole().getRoleAuthorities().isEmpty())
        .flatMap(staffRole -> staffRole.getRole().getRoleAuthorities().stream())
        .filter(ra -> Authority.AuthorityStatus.ACTIVE.equals(ra.getAuthority().getStatus()))
        .map(ra -> Stream.<Optional<String>>of(
            ra.isRead()
            ? Optional.of(Privilege.read(ra.getAuthority().getCode()))
            : Optional.empty(),
            ra.isWrite()
            ? Optional.of(Privilege.write(ra.getAuthority().getCode()))
            : Optional.empty(),
            ra.isExec()
            ? Optional.of(Privilege.execute(ra.getAuthority().getCode()))
            : Optional.empty()
        ))
        .flatMap(Stream::distinct)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toUnmodifiableSet());
  }
}
