package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.ScheduleCriticalDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScheduleCriticalDefinitionRepository extends JpaRepository<ScheduleCriticalDefinition, Long> {

    @Query
    List<ScheduleCriticalDefinition> findByProcessId(@Param("processId") Long processId);

    List<ScheduleCriticalDefinition> findByIdAndProcessId(Long id, Long processId);

    List<ScheduleCriticalDefinition> findByProcessIdOrderByScheduleEndDateDesc(Long processId);

}
