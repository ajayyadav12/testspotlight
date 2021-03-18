package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.ParentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ParentSubmissionRepository
        extends JpaRepository<ParentSubmission, Long>, JpaSpecificationExecutor<ParentSubmission> {

    List<ParentSubmission> findByProcessIdAndEndTimeIsNullOrderByStartTimeDesc(Long processId);

    List<ParentSubmission> findByProcessIdAndStartTimeIsGreaterThanOrderByIdDesc(Long processId, Date yesterday);

    List<ParentSubmission> findByStartTimeIsBetweenOrderByStartTimeAsc(Date start, Date end);

    List<ParentSubmission> findByProcessIdAndStartTimeIsBetweenOrderByStartTimeAsc(Long processId, Date from, Date to);

    Optional<ParentSubmission> findFirstByProcessIdAndEndTimeIsNullOrderByStartTimeDesc(Long processId);

    Optional<ParentSubmission> findFirstByProcessIdAndStartTimeIsGreaterThanOrderByIdDesc(Long processId, Date yesterday);

}
