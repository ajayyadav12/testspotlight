package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.User;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepository extends CrudRepository<User, Long> {

    @Override
    @Query
    List<User> findAll();

    User findFirstBySso(Long sso);

    List<User> findByRoleId(Long roleId);

}
