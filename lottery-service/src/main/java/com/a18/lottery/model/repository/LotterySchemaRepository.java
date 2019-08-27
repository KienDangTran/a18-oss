package com.a18.lottery.model.repository;

import com.a18.lottery.model.LotterySchema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LotterySchemaRepository
    extends JpaRepository<LotterySchema, Integer>, JpaSpecificationExecutor<LotterySchema> {

  @RestResource(exported = false)
  @Override <S extends LotterySchema> S save(S entity);

  @RestResource(exported = false)
  @Override void delete(LotterySchema entity);
}
