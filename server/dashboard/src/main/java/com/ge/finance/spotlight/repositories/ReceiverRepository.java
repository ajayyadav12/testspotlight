package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.Receiver;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ReceiverRepository extends CrudRepository<Receiver, Long> {

    @Override
    @Query
    List<Receiver> findAll();

    Long countByNameIgnoreCase(String name);
}
