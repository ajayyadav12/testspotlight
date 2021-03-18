package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.SubmissionStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionStepRepository extends JpaRepository<SubmissionStep, Long> {

    List<SubmissionStep> findBySubmissionIdOrderByStartTimeAsc(Long submissionId);

    List<SubmissionStep> findBySubmissionIdOrderByIdAsc(Long submissionId);

    List<SubmissionStep> findBySubmissionId(Long submissionId);

    // List<SubmissionStep> findBySubmissionIdOrderByIdAsc(List<Long>
    // submissionIds);

    List<SubmissionStep> findBySubmissionIdAndEndTimeIsNullOrderByStartTimeDesc(Long submissionId);

    List<SubmissionStep> findByProcessStepIdAndEndTimeIsNotNull(Long processStepId);

    List<SubmissionStep> findByProcessStepId(Long processStepId);

    boolean existsByProcessStepId(Long processStepid);

}
