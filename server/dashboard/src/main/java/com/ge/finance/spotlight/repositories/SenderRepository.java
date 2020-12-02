package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.Sender;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SenderRepository extends CrudRepository<Sender, Long> {

    @Override
    @Query
    List<Sender> findAll();
    
    Long countByNameIgnoreCase(String name);

}
