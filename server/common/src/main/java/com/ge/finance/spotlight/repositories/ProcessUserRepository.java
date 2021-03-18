package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.ProcessUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ProcessUserRepository extends JpaRepository<ProcessUser, Long> {

    @Query
    List<ProcessUser> findByProcessId(@Param("processId") Long processId);

    List<ProcessUser> findByUserId(Long userId);

    List<ProcessUser> findByUserIdAndProcessId(Long userId, Long processId);

    @Query("select pu from ProcessUser pu join pu.user u where u.sso = :sso")
    Collection<ProcessUser> findByUserSSO(@Param("sso") Long sso);

    boolean existsByUserId(Long userId);

    boolean existsByProcessId(Long processId);

}
