package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.Role;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RoleRepository extends CrudRepository<Role, Long> {

    @Override
    List<Role> findAll();

}
