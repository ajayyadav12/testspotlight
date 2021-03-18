package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.ProcessExportRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface ProcessExportRequestRepository extends JpaRepository<ProcessExportRequest, Long> {

    Optional<ProcessExportRequest> findFirsByIdAndProcessId(Long id, Long processId);

    Optional<ProcessExportRequest> findFirstByIdAndProcessIdAndUserId(Long id, Long processId, Long userId);

    Collection<ProcessExportRequest> findByProcessId(Long processId);

    Collection<ProcessExportRequest> findByProcessIdAndUserId(Long processId, Long userId);

    Optional<ProcessExportRequest> existsByProcessId(Long processId);

}
