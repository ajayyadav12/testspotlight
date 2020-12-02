package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.ClosePhase;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ClosePhaseRepository extends CrudRepository<ClosePhase, Long> {

    @Override
    List<ClosePhase> findAll();

}
