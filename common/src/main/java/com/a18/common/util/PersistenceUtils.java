package com.a18.common.util;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class PersistenceUtils<T extends Serializable, ID extends Serializable> {

  @PersistenceContext private EntityManager entityManager;

  public T getPersistedEntity(ID id, Class<T> entityClazz) {
    this.entityManager.clear();
    CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
    CriteriaQuery<T> criteriaQuery = cb.createQuery(entityClazz);
    Root<T> root = criteriaQuery.from(entityClazz);
    criteriaQuery.select(root)
                 .where(cb.equal(root.get("id"), id));
    TypedQuery<T> query = this.entityManager.createQuery(criteriaQuery);
    List<T> rs = query.getResultList();

    if (rs.size() <= 0) {
      return null;
    }

    return rs.get(0);
  }
}
