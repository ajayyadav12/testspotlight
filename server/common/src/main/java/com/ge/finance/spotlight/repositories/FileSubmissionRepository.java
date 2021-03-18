package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.FileSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileSubmissionRepository extends JpaRepository<FileSubmission, Long> {

    boolean existsBySubmissionIdAndSecret(Long submissionId, String secret);

}
