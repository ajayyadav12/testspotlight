package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.ProcessUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProcessUserRepository extends JpaRepository<ProcessUser, Long> {

    @Query
    List<ProcessUser> findByProcessId(@Param("processId") Long processId);

    List<ProcessUser> findByUserId(Long userId);

    List<ProcessUser> findByUserIdAndProcessId(Long userId, Long processId);

    long countByUserId(Long userId);

    long countByProcessId(Long processId);

}
