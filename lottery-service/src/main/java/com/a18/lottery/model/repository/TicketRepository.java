package com.a18.lottery.model.repository;

import com.a18.common.constant.Ccy;
import com.a18.common.constant.Privilege;
import com.a18.lottery.model.Ticket;
import com.a18.lottery.model.Ticket.TicketStatus;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;

public interface TicketRepository
    extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

  @PostAuthorize("hasAuthority('" + Privilege.READ + Privilege.TICKET + "') "
                     + "or returnObject.get().username.equals(authentication.name)")
  @Override Optional<Ticket> findById(@NotNull Long id);

  @PreAuthorize("hasAuthority('" + Privilege.WRITE + Privilege.TICKET + "') "
                    + "or (#entity.username.equals(authentication.name) "
                    + "and 'NEW'.equalsIgnoreCase(#entity.status.name()))")
  @Override <S extends Ticket> S save(@NotNull @P("entity") S entity);

  @PreAuthorize("#entity.username.equals(authentication.name) "
                    + "and 'NEW'.equalsIgnoreCase(#entity.status.name())")
  @Override void delete(@NotNull @P("entity") Ticket entity);

  long countAllByIssueIdAndStatusIn(@NotNull Long issueId, @NotEmpty Set<TicketStatus> statuses);

  boolean existsByIssueIdAndLotteryIdAndCcyAndUsername(
      @NotNull Long issueId,
      @NotNull Integer lotteryId,
      @NotNull Ccy ccy,
      @NotNull String username
  );

  @RestResource(exported = false)
  Stream<Ticket> getAllByIssueId(Long issueId);
}
