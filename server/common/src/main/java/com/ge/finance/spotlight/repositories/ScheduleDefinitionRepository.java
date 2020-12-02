package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.ScheduleDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScheduleDefinitionRepository extends JpaRepository<ScheduleDefinition, Long> {

    @Query
    List<ScheduleDefinition> findByProcessId(@Param("processId") Long processId);

    List<ScheduleDefinition> findByIdAndProcessId(Long id, Long processId);

    List<ScheduleDefinition> findByProcessIdOrderByScheduleEndDateDesc(Long processId);

}
