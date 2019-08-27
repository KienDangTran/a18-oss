package com.a18.lottery.model.repository;

import com.a18.lottery.model.BetItemGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface BetItemGroupRepository
    extends JpaRepository<BetItemGroup, Long>, JpaSpecificationExecutor<BetItemGroup> {}
