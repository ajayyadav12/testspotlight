package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.Status;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StatusRepository extends JpaRepository<Status, Long> {

    Optional<Status> findFirstByName(String name);

    Optional<Status> findFirstByNameIgnoreCase(String name);

}
