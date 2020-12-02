package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.Status;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatusRepository extends JpaRepository<Status, Long> {

    @Cacheable("statusByName")
    List<Status> findByNameIgnoreCase(String name);

}
