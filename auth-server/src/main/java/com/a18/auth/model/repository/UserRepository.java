package com.a18.auth.model.repository;

import com.a18.auth.model.User;
import com.a18.auth.model.UserDocument;
import com.a18.common.constant.Privilege;
import java.util.Optional;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;

public interface UserRepository
    extends UserDetailsRepository<User, Long>, JpaSpecificationExecutor<User> {

  @PostAuthorize("hasAuthority('" + Privilege.READ + Privilege.USER + "') "
                     + "or (returnObject.isPresent() "
                     + "and returnObject.get().username.equals(authentication.name))")
  @Override Optional<User> findById(@NotNull Long id);

  @PreAuthorize("hasAuthority('" + Privilege.WRITE + Privilege.USER + "') "
                    + "or #entity.username.equals(authentication.name)")
  @Override <S extends User> S save(@NotNull @P("entity") S entity);

  @PreAuthorize("hasAuthority('" + Privilege.EXEC + Privilege.USER + "')")
  @Override void delete(@NotNull User user);

  @Query("SELECT u "
             + "FROM User u JOIN u.userDocuments d "
             + "WHERE d.cardNo = :cardNo AND d.type = :type")
  Optional<User> findByDocumentCardNoAndType(
      @NotEmpty @Param("cardNo") String cardNo,
      @NotNull @Param("type") UserDocument.DocumentType type
  );
}
