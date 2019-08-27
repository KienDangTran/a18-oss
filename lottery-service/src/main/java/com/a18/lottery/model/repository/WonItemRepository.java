package com.a18.lottery.model.repository;

import com.a18.lottery.model.WonItem;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface WonItemRepository extends JpaRepository<WonItem, Long> {
  Set<WonItem> findAllByTicketIdAndAndBetContentIn(Long ticketId, Set<String> betContents);
}
