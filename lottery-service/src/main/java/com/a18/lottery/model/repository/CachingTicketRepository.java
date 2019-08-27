package com.a18.lottery.model.repository;

import com.a18.common.constant.Ccy;
import com.a18.lottery.model.cache.CachingTicket;
import java.util.Optional;
import java.util.Set;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface CachingTicketRepository extends CrudRepository<CachingTicket, Long> {
  Set<CachingTicket> findAllByIssueId(@NotNull Long issueId);

  Optional<CachingTicket> findByIssueIdAndLotteryIdAndCcyAndUsername(
      @NotNull Long issueId,
      @NotNull Integer lotteryId,
      @NotNull Ccy ccy,
      @NotEmpty String username
  );
}
