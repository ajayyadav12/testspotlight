package com.ge.finance.spotlight.repositories;

import java.util.Date;
import java.util.List;

import com.ge.finance.spotlight.models.SubmissionRequest;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRequestRepository extends JpaRepository<SubmissionRequest, Long> {

    List<SubmissionRequest> findByStateOrderByStartTimeAsc(String state);

}