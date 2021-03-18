package com.ge.finance.spotlight.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import com.ge.finance.spotlight.models.ProcessParentChild;

public interface ProcessParentChildRepository extends JpaRepository<ProcessParentChild, Long> {

        List<ProcessParentChild> findByProcessIdOrderBySeqAscIdAsc(long processId);

        boolean existsByProcessId(long processId);

        // boolean existsByChildId(long childId);

       @Query(value = "SELECT TOP 1 * from spotlight_dba.t_process_parent_child"
                        + " WHERE process_id = :parentId and seq > (SELECT seq from spotlight_dba.t_process_parent_child WHERE child_id = :childId)", nativeQuery = true)
        ProcessParentChild findSuccessorByProcessIdAndChildId(@Param("parentId") Long parentId,
                        @Param("childId") Long childId);

        @Query(value = "SELECT  TOP 1 * from spotlight_dba.t_process_parent_child"
                        + " WHERE process_id = :parentId and seq < (SELECT seq from spotlight_dba.t_process_parent_child WHERE child_id = :childId)", nativeQuery = true)
        ProcessParentChild findPredecessorByProcessIdAndChildId(@Param("parentId") Long parentId,
                        @Param("childId") Long childId);

        @Query(value = "SELECT * from spotlight_dba.t_process_parent_child WHERE child_id = :childId ", nativeQuery = true)
        List<ProcessParentChild> findByChildId(@Param("childId") Long childId);

}
