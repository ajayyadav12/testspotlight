package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.User;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Optional;

public class UserRepositoryExtensionImpl implements UserRepositoryExtension {

    @PersistenceContext private EntityManager entityManager;

    @Override
    public Optional<User> findOptionalBySSO(Long sso) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = criteriaBuilder.createQuery(User.class);
        Root<User> root = query.from(User.class);
        query.where(criteriaBuilder.equal(root.<Long>get("sso"), sso));
        try {
            User user = entityManager.createQuery(query).getSingleResult();
            return Optional.of(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

}
