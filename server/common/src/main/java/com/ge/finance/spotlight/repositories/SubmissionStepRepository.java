package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.SubmissionStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionStepRepository extends JpaRepository<SubmissionStep, Long> {

    List<SubmissionStep> findBySubmissionIdOrderByIdAsc(Long submissionId);

    List<SubmissionStep> findBySubmissionIdAndEndTimeIsNullOrderByStartTimeDesc(Long submissionId);

    List<SubmissionStep> findByProcessStepIdAndEndTimeIsNotNull(Long processStepId);

    boolean existsByProcessStepId(Long processStepid);

}
