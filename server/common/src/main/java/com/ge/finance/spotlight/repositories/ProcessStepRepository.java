package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.ProcessStep;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProcessStepRepository extends CrudRepository<ProcessStep, Long> {

    @Query
    List<ProcessStep> findByProcessId(@Param("processId") Long processId);

    List<ProcessStep> findByProcessIdAndNameIgnoreCase(Long processId, String name);

    @Query("select case when count(s) > 0 then true else false end from ProcessStep s where s.id = :id and s.processId = :processId")
    boolean existsByIdAndProcessId(@Param("id") Long id, @Param("processId") Long processId);

    long countByProcessId(Long processId);

}
