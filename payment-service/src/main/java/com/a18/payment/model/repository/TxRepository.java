package com.a18.payment.model.repository;

import com.a18.payment.model.Tx;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface TxRepository extends JpaRepository<Tx, Long>, JpaSpecificationExecutor<Tx> {
  @Query("SELECT SUM(tx.amt) FROM Tx tx WHERE tx.username = :username AND tx.status = 'PENDING'")
  BigDecimal sumTotalPendingTxAmt(String username);
}
