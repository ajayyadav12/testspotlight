package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.ModuleFilter;
import com.ge.finance.spotlight.models.User;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import java.util.List;

public class ModuleFilterRepositoryExtensionImpl implements ModuleFilterRepositoryExtension {

    @PersistenceContext private EntityManager entityManager;

    @Override
    public boolean existsAllForIdListAndSso(List<Long> idList, Long sso) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Boolean> query = cb.createQuery(Boolean.class);
        Root<ModuleFilter> root = query.from(ModuleFilter.class);
        EntityType<ModuleFilter> ModuleFilter_ = entityManager.getMetamodel().entity(ModuleFilter.class);
        Join<ModuleFilter, User> join = root.join(ModuleFilter_.getSingularAttribute("user", User.class));
        query.select(cb.<Boolean>selectCase()
                .when(cb.equal(cb.count(root), idList.size()), Boolean.TRUE)
                .otherwise(Boolean.FALSE)
        ).where(cb.and(root.get("id").in(idList), cb.equal(join.<Long>get("sso"), sso)));
        return entityManager.createQuery(query).getSingleResult();
    }

}