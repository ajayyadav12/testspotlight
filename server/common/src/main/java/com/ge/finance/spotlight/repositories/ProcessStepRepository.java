package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.ProcessStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProcessStepRepository extends JpaRepository<ProcessStep, Long> {

    @Query
    List<ProcessStep> findByProcessIdAndAssociatedStepIdIsNull(@Param("processId") Long processId);

    @Query
    List<ProcessStep> findByProcessId(@Param("processId") Long processId);

    @Query
    List<ProcessStep> findByProcessIdOrderByIdDesc(@Param("processId") Long processId);

    List<ProcessStep> findByProcessIdAndNameIgnoreCase(Long processId, String name);

    Optional<ProcessStep> findFirstByNameAndProcessId(String name, Long processId);

    @Query("select case when count(s) > 0 then true else false end from ProcessStep s where s.id = :id and s.processId = :processId")
    boolean existsByIdAndProcessId(@Param("id") Long id, @Param("processId") Long processId);

    @Query("select case when count(s) > 0 then true else false end from ProcessStep s where s.processId = :id and s.name = :name")
    boolean existsByNameAndProcessId(@Param("id") Long id, @Param("name") String name);

    @Query("select case when count(s) > 0 then true else false end from ProcessStep s where s.id = :id and s.name = :name")
    boolean existsByNameAndProcessStepId(@Param("id") Long id, @Param("name") String name);

    boolean existsByProcessId(Long processId);

    Optional<ProcessStep> findFirstByProcessIdAndNameIgnoreCase(Long processId, String name);

}
