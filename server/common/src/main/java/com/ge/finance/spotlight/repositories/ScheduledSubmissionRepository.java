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
import org.springframework.data.repository.query.Param;

public interface ScheduledSubmissionRepository extends CrudRepository<ScheduledSubmission, Long>,
                JpaRepository<ScheduledSubmission, Long>, JpaSpecificationExecutor<ScheduledSubmission> {

        List<ScheduledSubmission> findByStartTimeIsBetweenAndProcessIdIsInAndSubmissionIdIsNullOrderByStartTimeAsc(
                        Date start, Date end, List<Long> processIdList);

        List<ScheduledSubmission> findByStartTimeIsBetweenAndProcessIdIsInOrderByStartTimeAsc(Date start, Date end,
                        List<Long> processIdList);

        List<ScheduledSubmission> findByStartTimeIsBetweenAndProcessIdIsInOrderByEndTimeAsc(Date start, Date end,
                        List<Long> processIdList);

        List<ScheduledSubmission> findByStartTimeIsBetweenAndSubmissionIdIsNullOrderByStartTimeAsc(Date start,
                        Date end);

        List<ScheduledSubmission> findByProcessIdAndStartTimeIsBetweenAndSubmissionIsNullOrderByStartTimeDesc(
                        Long processId, Date startTime, Date endTime);

        List<ScheduledSubmission> findByProcessIdAndStartTimeIsBetweenAndSubmissionIsNullAndPredecessorSchSubIdIsNotNullOrderByStartTimeDesc(
                        Long processId, Date startTime, Date endTime);

        Optional<ScheduledSubmission> findBySubmissionId(Long submissionId);

        Optional<ScheduledSubmission> findFirstByProcessIdAndStartTimeIsBetweenAndSubmissionIsNullOrderByStartTimeDesc(
                        Long processId, Date startTime, Date endTime);

        Optional<ScheduledSubmission> findFirstByProcessIdAndStartTimeIsBetweenAndSubmissionIsNullAndPredecessorSchSubIdIsNotNullOrderByStartTimeDesc(
                        Long processId, Date startTime, Date endTime);

        List<ScheduledSubmission> findByProcessIdIn(List<Long> processIdList);

        List<ScheduledSubmission> findByProcessId(long processId);

        List<ScheduledSubmission> findByProcessIdInAndPredecessorSchSubIdIsNotNull(List<Long> processIdList);

        @Transactional
        void deleteByScheduleDefinitionId(Long scheduleDefinitionId);

        List<ScheduledSubmission> findByStartTimeIsBetweenAndScheduleDefinitionIdIsOrderByStartTimeAsc(Date start,
                        Date end, Long scheduleDefId);

        @Query(value = "SELECT ss.id, ss.start_time , ss.end_time," + " ss.acknowledgement_flag , ss.disabled_flag "
                        + " FROM spotlight_dba.t_notification_log nl, spotlight_dba.t_scheduled_submission ss "
                        + " WHERE (nl.id) in " + " (SELECT MAX(id) " + " FROM spotlight_dba.t_notification_log "
                        + " WHERE (sendtime) > (GETDATE() - :days) " + " and notification_template_id in (998) "
                        + " and submission_id is null " + " group by scheduled_submission_id) "
                        + " AND (nl.scheduled_submission_id) in " + " (SELECT (scheduled_submission_id "
                        + " FROM spotlight_dba.t_notification_log " + " WHERE (sendtime) > (GETDATE() - :days) "
                        + " AND notification_template_id in (998) " + " AND submission_id is null "
                        + " GROUP BY scheduled_submission_id) " + " AND notification_template_id=998 "
                        + " AND ss.id= nl.scheduled_submission_id " + " AND (ss.start_time) > (GETDATE() - :days) "
                        + " AND ss.schedule_def_id= :scheduleDefId"
                        + " ORDER BY ss.start_time DESC ", nativeQuery = true)
        List<?> findDelayedSubmissions(@Param("days") Integer days, @Param("scheduleDefId") Long scheduleDefId);

}
