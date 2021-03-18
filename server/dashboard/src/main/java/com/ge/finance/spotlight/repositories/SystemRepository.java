package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.System;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SystemRepository extends CrudRepository<System, Long> {

    @Override
    @Query
    List<System> findAll();
    
    Long countByNameIgnoreCase(String name);

    List<System> findByClosePhaseIdOrderByNameAsc(long closePhaseId);

    /**
     * Get all systems that are either receivers or senders of {systemId} and are not Middleware
     * @param systemId
     * @return
     */
    @Query(value ="SELECT s.id, s.name, 'S' DIRECTION,"
    + " CASE p.is_parent"
        + " WHEN 'N' THEN"
            + " (SELECT top(1) CONVERT(VARCHAR, ts.status_id) + ',' + CONVERT(VARCHAR, ts.id) FROM spotlight_dba.t_submission ts"            
            + " WHERE ts.process_id = p.id"            
            + " AND ( ( :timeOption = 1 ) OR ( :timeOption = 2 AND (ts.start_time) >= (GETDATE()-1) ) OR (:timeOption = 3 AND (ts.start_time) = (GETDATE())) )"
            + " ORDER BY ts.id desc)"
        + " WHEN 'Y' THEN"
            + " (SELECT top(1) CONVERT(VARCHAR, tps.status_id) +','+ CONVERT(VARCHAR, tps.id) FROM spotlight_dba.t_parent_submission tps"             
            + " WHERE tps.process_id = p.id"             
            + " AND ( ( :timeOption = 1 ) OR ( :timeOption = 2 AND (tps.start_time) >= (GETDATE()-1) ) OR (:timeOption = 3 AND (tps.start_time) = (GETDATE())) )"
            + " ORDER BY tps.id desc)"
    + " END"
      + " STATUS, p.name PROCESS, p.is_parent"
  + " from spotlight_dba.t_process p"
  + " JOIN spotlight_dba.t_system s ON p.sender_id = s.id"
  + " WHERE p.receiver_id = :systemId AND s.close_phase_id != 7 AND p.receiver_id != sender_id"
  + " UNION ALL"
  + " SELECT id, name, '0' DIRECTION, '0,0' STATUS, '' PROCESS, '' from spotlight_dba.t_system"
  + " WHERE id = :systemId"
  + " UNION ALL"
  + " SELECT s.id, s.name, 'R' DIRECTION," 
    + " CASE p.is_parent" 
        + " WHEN 'N' THEN"
            + " (SELECT top(1) CONVERT(VARCHAR, ts.status_id) + ',' + CONVERT(VARCHAR, ts.id) FROM spotlight_dba.t_submission ts"             
            + " WHERE ts.process_id = p.id"
            + " AND ( ( :timeOption = 1 ) OR ( :timeOption = 2 AND (ts.start_time) >= (GETDATE()-1) ) OR (:timeOption = 3 AND (ts.start_time) = (GETDATE())) )"
            + " ORDER BY ts.id desc)"
        + " WHEN 'Y' THEN"
            + " (SELECT top(1) CONVERT(VARCHAR, tps.status_id) +','+ CONVERT(VARCHAR, tps.id) FROM spotlight_dba.t_parent_submission tps"            
            + " WHERE tps.process_id = p.id"
            + " AND ( ( :timeOption = 1 ) OR ( :timeOption = 2 AND (tps.start_time) >= (GETDATE()-1) ) OR (:timeOption = 3 AND (tps.start_time) = (GETDATE())) )"
            + " ORDER BY tps.id desc)"
    + " END"
      + " STATUS, p.name PROCESS, p.is_parent"
  + " from spotlight_dba.t_process p"
  + " JOIN spotlight_dba.t_system s ON p.receiver_id = s.id"
  + " WHERE p.sender_id = :systemId AND s.close_phase_id != 7 AND p.receiver_id != sender_id"
    , nativeQuery = true)
    List<?> findSystemRelationships(@Param("systemId") Integer systemId, @Param("timeOption") Integer timeOption);

    Optional<System> findFirstByName(String name);
}
