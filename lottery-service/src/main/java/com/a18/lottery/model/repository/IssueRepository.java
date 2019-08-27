package com.a18.lottery.model.repository;

import com.a18.lottery.model.Issue;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

@RepositoryRestResource
public interface IssueRepository
    extends JpaRepository<Issue, Long>, JpaSpecificationExecutor<Issue> {

  @RestResource(exported = false)
  @Override <S extends Issue> S save(S entity);

  @RestResource(exported = false)
  @Override void delete(Issue entity);

  @Query("select distinct i "
             + " from Issue i "
             + " where i.schedulerId = ?1"
             + " and i.openingTime <= ?2 and i.closingTime > ?2"
             + " and i.status in ?3")
  Optional<Issue> findIssueByTimeAndStatus(
      Integer schedulerId,
      LocalDateTime openingTime,
      Set<Issue.IssueStatus> statuses
  );

  int countAllBySchedulerIdAndCode(Integer scheduleId, String code);

  long countAllBySchedulerIdAndOpeningTimeBetween(
      Integer scheduleId,
      LocalDateTime startTime,
      LocalDateTime endTime
  );

  @Query("select count(i.id) "
             + " from Issue i "
             + " where i.schedulerId = ?1 and i.openingTime <= ?2 and i.closingTime > ?2")
  int countOverlapIssues(Integer scheduleId, LocalDateTime time);

  @RestResource(exported = false)
  Set<Issue> getAllBySchedulerIdAndClosingTimeBeforeAndStatusIn(
      Integer schedulerId,
      LocalDateTime time,
      Set<Issue.IssueStatus> statuses
  );

  Optional<Issue> getTopBySchedulerIdAndOpeningTimeGreaterThanEqualAndStatusInOrderByOpeningTimeAsc(
      Integer schedulerId,
      LocalDateTime time,
      Set<Issue.IssueStatus> statuses
  );
}
