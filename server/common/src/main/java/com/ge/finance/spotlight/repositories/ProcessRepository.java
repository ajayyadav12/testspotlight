package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.dto.ProcessDTO;
import com.ge.finance.spotlight.models.Process;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProcessRepository extends JpaRepository<Process, Long>, JpaSpecificationExecutor<Process> {

    @Override
    @Query
    List<Process> findAll();

    @Query
    List<Process> findByIdIsIn(@Param("processIdList") List<Long> processIdList);

    List<Process> findByProcessParentId(Long processParentId);

    long countByProcessParentId(Long processParentId);

    long countBySenderId(Long senderId);

    long countByReceiverId(Long receiverId);

    long countByAppOwnerId(Long ownerId);

    boolean existsByNameIgnoreCase(String name);

    @Query("SELECT new com.ge.finance.spotlight.dto.ProcessDTO (process.id, process.name, process.isParent) FROM Process process WHERE process.approved = 'A' ORDER BY process.name")
    List<ProcessDTO> findAllProcess();

    @Query("SELECT new com.ge.finance.spotlight.dto.ProcessDTO (process.id, process.name, process.isParent) FROM Process process WHERE process.approved = 'A' AND process.id in (:processIdList) ORDER BY process.name")
    List<ProcessDTO> findByIdAllProcess(@Param("processIdList") List<Long> processIdList);

}
