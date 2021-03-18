package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.User;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface UserRepository extends CrudRepository<User, Long>, UserRepositoryExtension {

    @Override
    @Query
    List<User> findAll();

    /**
     * @deprecated Avoid handling null values returning an optional here, use {@link #findOptionalBySSO(Long)} instead
     */
    @Deprecated
    User findFirstBySso(Long sso);

    List<User> findByRoleId(Long roleId);

    @Query("select u from User u join u.role r where r.description = :roleName")
    List<User> findUserByRoleName(@Param("roleName") String roleName);

    List<User> findByPhoneNumberNotNullAndCarrierNotNullOrderByName();

}
