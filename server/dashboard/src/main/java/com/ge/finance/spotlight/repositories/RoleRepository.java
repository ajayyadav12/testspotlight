package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.Role;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends CrudRepository<Role, Long> {

    @Override
    List<Role> findAll();

    Optional<Role> findFirstByDescription(String description);

}
