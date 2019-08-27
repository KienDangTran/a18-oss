package com.a18.lottery.model.repository;

import com.a18.lottery.model.PrizeSchema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RestResource;

public interface PrizeSchemaRepository extends JpaRepository<PrizeSchema, Integer> {

  @RestResource(exported = false)
  @Override <S extends PrizeSchema> S save(S entity);

  @RestResource(exported = false)
  @Override void delete(PrizeSchema entity);
}
