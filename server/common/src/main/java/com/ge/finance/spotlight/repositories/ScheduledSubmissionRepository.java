package com.ge.finance.spotlight.repositories;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import com.ge.finance.spotlight.models.ScheduledSubmission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface ScheduledSubmissionRepository extends CrudRepository<ScheduledSubmission, Long>,
                JpaRepository<ScheduledSubmission, Long>, JpaSpecificationExecutor<ScheduledSubmission> {

        List<ScheduledSubmission> findByStartTimeIsBetweenAndProcessIdIsInAndSubmissionIdIsNullOrderByStartTimeAsc(
                        Date start, Date end, List<Long> processIdList);

        List<ScheduledSubmission> findByStartTimeIsBetweenAndProcessIdIsInOrderByStartTimeAsc(Date start, Date end,
                        List<Long> processIdList);

        List<ScheduledSubmission> findByStartTimeIsBetweenOrderByStartTimeAsc(Date start, Date end);

        List<ScheduledSubmission> findByStartTimeIsBetweenAndSubmissionIdIsNullOrderByStartTimeAsc(Date start,
                        Date end);

        List<ScheduledSubmission> findByProcessIdAndStartTimeIsLessThanAndSubmissionIsNullOrderByStartTimeDesc(
                        Long processId, Date date);

        Optional<ScheduledSubmission> findBySubmissionId(Long submissionId);

        List<ScheduledSubmission> findByProcessIdIn(List<Long> processIdList);

        @Transactional
        void deleteByScheduleDefinitionId(Long scheduleDefinitionId);

        List<ScheduledSubmission> findByStartTimeIsBetweenAndScheduleDefinitionIdIsInOrderByStartTimeAsc(Date start,
                        Date end, Long scheduleDefId);

}
