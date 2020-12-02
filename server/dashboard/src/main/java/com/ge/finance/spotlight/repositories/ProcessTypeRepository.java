package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.ProcessType;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProcessTypeRepository extends CrudRepository<ProcessType, Long> {

    @Override
    @Query
    List<ProcessType> findAll();

}
