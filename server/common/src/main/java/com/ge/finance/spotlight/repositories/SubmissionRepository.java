package com.ge.finance.spotlight.repositories;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.ge.finance.spotlight.models.Submission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface SubmissionRepository extends CrudRepository<Submission, Long>, JpaRepository<Submission, Long>,
                JpaSpecificationExecutor<Submission> {

        // boolean existByProcessId(Long processId);

        List<Submission> findByProcessId(long processId);

        @Query("select p.id from Submission s join s.process p where s.id = :id")
        Optional<Long> findProcessIdForSubmissionId(@Param("id") Long submissionId);

        List<Submission> findByProcessIdAndStatusId(Long processId, Long statusId);

        Optional<Long> countByProcessIdAndStartTimeGreaterThan(Long processId, Date startTime);

        Optional<Long> countByProcessIdAndStatusIdAndStartTimeGreaterThan(Long processId, Long statusId,
                        Date startTime);

        @Query("select SUM(s.records) from Submission s join s.process p where p.id = :processId and s.startTime > :startTime")
        Optional<Long> totalRecordsCountForProcessId(@Param("processId") Long processId,
                        @Param("startTime") Date startTime);

        @Query("SELECT DATEDIFF(minute,s.endTime, s.startTime) * 1440 FROM Submission s join s.process p  WHERE p.id = :processId and status_id= :statusId and s.startTime > :startTime AND s.endTime IS NOT null")
        List<Double> findRunTimeForProcessId(@Param("processId") Long processId, @Param("statusId") Long statusId,
                        @Param("startTime") Date startTime);

        List<Submission> findByProcessIdAndEndTimeIsNullOrderByStartTimeDesc(Long processId);

        List<Submission> findByStartTimeIsBetweenOrderByStartTimeAsc(Date start, Date end);

        List<Submission> findByProcessIdAndStartTimeIsBetweenOrderByStartTimeAsc(Long processId, Date from, Date to);

        @Query("SELECT c FROM Submission c WHERE c.process.id = :processId and c.startTime between :from AND :to and "
                        + "(c.bu = '' or :businessUnit is null or c.bu = :businessUnit)")
        List<Submission> findByProcessIdAndBu(@Param("processId") Long processId,
                        @Param("businessUnit") String businessUnit, @Param("from") Date from, @Param("to") Date to);

        List<Submission> findByParentOrderByStartTimeAsc(Long parentId);

        @Query("select s from Submission s WHERE s.process.submissionEscalationAlrt = :submissionEscalationAlrt " 
                        + " AND s.status.id = :statusId AND s.acknowledgementFlag = :acknowledgementFlag " 
                        + " AND s.ackFailedEmailStatus != :ackFailedEmailStatus " 
                        + " AND s.startTime > :yesterday")
        List<Submission> findByProcessSubmissionEscalationAlrtAndStatusIdAndAcknowledgementFlagAndAckFailedEmailStatusIsNot(
                                        @Param("submissionEscalationAlrt") Character submissionEscalationAlrt, 
                                        @Param("statusId") Long statusId, 
                                        @Param("acknowledgementFlag") boolean acknowledgementFlag, 
                                        @Param("ackFailedEmailStatus") Character ackFailedEmailStatus,
                                        @Param("yesterday") Date yesterday);

        Submission findFirstByProcessIdAndStatusIdAndAckFailedEmailStatusIsNotAndAcknowledgementFlagTrueOrderByIdDesc(
                        Long processId, Long statusId, Character ackFailedEmailStatus);

        Submission findFirstByProcessIdAndStatusIdIsNotAndIdGreaterThan(Long processId, Long statusId, Long id);

        Optional<Submission> findFirstByProcessIdAndAltIdIgnoreCaseAndEndTimeIsNullOrderByIdDesc(Long processId,
                        String altId);

        List<Submission> findByEndTimeIsNullAndStartTimeGreaterThanOrderByStartTimeDesc(Date date);

        Optional<Submission> findFirstByProcessIdAndIdIsNotOrderByStartTimeDesc(Long processId, Long id);

        Optional<Submission> findFirstByProcessIdAndEndTimeIsNullOrderByStartTimeDesc(Long processId);

        @Query(value = "SELECT TIME, ISNULL([Failed], 0) AS FAILED_SUM_COUNT, ISNULL([Warning], 0) AS WARNING_SUM_COUNT, ISNULL([in progress], 0) AS LONG_RUNNING_SUM_COUNT, ISNULL([success], 0) AS DELAYED_SUM_COUNT, ISNULL([unacknowledged],0) AS SUCCESS_SUM_COUNT, ISNULL([Long Running],0) AS unacknowledged_SUM_COUNT , ISNULL([Delayed],0) AS IN_PROGRESS_SUM_COUNT, ISNULL([scheduled_unacknowledged],0) AS scheduled_unack_SUM_COUNT  FROM "
                        + "  (SELECT CONVERT(Date, start_time) as TIME, CASE status_id  "
                        + "    WHEN 3 THEN 'Warning' WHEN 4 THEN 'Failed'  "
                        + "    WHEN 1 THEN 'in progress' WHEN 2 THEN 'success' WHEN 6 THEN 'in progress' END as STATUS, COUNT (*) as COUNT  "
                        + "    FROM spotlight_dba.t_submission s JOIN spotlight_dba.t_process p ON s.process_id = p.id  "
                        + "    WHERE status_id in (3, 4, 1, 2 , 6) AND (start_time) > (GETDATE() -:days)  "
                        + "    AND ( -1 in :processList OR p.id in :processList )  "
                        + "    AND ( -1 in :senderList OR p.sender_id in :senderList )  "
                        + "    AND ( -1 in :receiverList OR p.receiver_id in :receiverList )  "
                        + "    AND ( -1 in :parentList OR p.process_parent_id in :parentList )  "
                        + "    AND ( :bu = '-1' OR s.bu = :bu)  AND ( :adHoc = '-1' OR s.ad_hoc_flag = :adHoc )  "
                        + "    GROUP BY CONVERT(DATE, start_time), status_id  UNION ALL  "
                        + "  SELECT CONVERT(Date, sendtime) as TIME, CASE notification_template_id WHEN 998 THEN 'Delayed' WHEN 3 THEN 'Delayed' END as STATUS, 1 as COUNT  "
                        + "   FROM spotlight_dba.t_scheduled_submission ss, spotlight_dba.t_notification_log nl JOIN spotlight_dba.t_process p ON nl.process_id = p.id  "
                        + "   LEFT JOIN spotlight_dba.t_submission s ON nl.submission_id = s.id  "
                        + "   WHERE (nl.id) in  "
                        + "          (select max(id) "
                        + "             from spotlight_dba.t_notification_log  "
                        + "             where (sendtime) > (GETDATE()  - :days)  "
                        + "             and notification_template_id in (998)  "
                        + "             and submission_id is null  "
                        + "             group by scheduled_submission_id)  "
                        + "   AND (nl.scheduled_submission_id) in  "
                        + "          (SELECT scheduled_submission_id  "
                        + "             from spotlight_dba.t_notification_log  "
                        + "             where (sendtime) > (GETDATE()  - :days)  "
                        + "             and notification_template_id in (998)  "
                        + "             and submission_id is null  "
                        + "             group by scheduled_submission_id)  "
                        + "         AND ss.id= nl.scheduled_submission_id  "
                        + "         AND notification_template_id in (998)  "
                        + "         AND (sendtime) > (GETDATE() -:days)  "
                        + "         AND ( -1 in :processList OR p.id in :processList )  "
                        + "         AND ( -1 in :senderList OR p.sender_id in :senderList )  "
                        + "         AND ( -1 in :receiverList OR p.receiver_id in :receiverList )  "
                        + "         AND ( -1 in :parentList OR p.process_parent_id in :parentList )  "
                        + "         AND ( :bu = '-1' OR s.bu = :bu)  AND ( :adHoc = '-1' OR s.ad_hoc_flag = :adHoc )  "
                        + " GROUP BY CONVERT(DATE, sendtime), notification_template_id, nl.scheduled_submission_id "
                        + "     UNION ALL  "
                        + "   SELECT CONVERT(Date, sendtime) as TIME, CASE notification_template_id WHEN 997 THEN 'Long Running' END as STATUS, 1 as COUNT  "
                        + "    FROM spotlight_dba.t_notification_log nl JOIN spotlight_dba.t_process p ON nl.process_id = p.id  "
                        + "    LEFT JOIN spotlight_dba.t_submission s ON nl.submission_id = s.id  "
                        + "    WHERE notification_template_id in (997)  "
                        + "          AND (sendtime) > (GETDATE() -:days) " + "          AND s.end_time is null "
                        + "          AND ( -1 in :processList OR p.id in :processList )  "
                        + "          AND ( -1 in :senderList OR p.sender_id in :senderList )  "
                        + "          AND ( -1 in :receiverList OR p.receiver_id in :receiverList )  "
                        + "          AND ( -1 in :parentList OR p.process_parent_id in :parentList ) "
                        + "          AND ( :bu = '-1' OR s.bu = :bu)  AND ( :adHoc = '-1' OR s.ad_hoc_flag = :adHoc )   "
                        + "  GROUP BY CONVERT(DATE, sendtime), notification_template_id, nl.submission_id    " + " UNION ALL  "
                        + " SELECT CONVERT(Date, start_time) as TIME, CASE acknowledgement_flag  "
                        + "          WHEN '0' THEN 'unacknowledged'          WHEN null THEN 'unacknowledged'  "
                        + "          WHEN '1' THEN 'acknowledged'  END as status, COUNT(*) as COUNT  "
                        + "   From spotlight_dba.T_SUBMISSION s JOIN spotlight_dba.t_process p ON s.process_id = p.id  "
                        + "   WHERE status_id in (4) AND acknowledgement_flag <> 1  "
                        + "         AND (start_time) > (GETDATE() -:days)  "
                        + "         AND ( -1 in :processList OR p.id in :processList )  "
                        + "         AND ( -1 in :senderList OR p.sender_id in :senderList )  "
                        + "         AND ( -1 in :receiverList OR p.receiver_id in :receiverList )  "
                        + "         AND ( -1 in :parentList OR p.process_parent_id in :parentList )  "
                        + "         AND ( :bu = '-1' OR s.bu = :bu) AND ( :adHoc = '-1' OR s.ad_hoc_flag = :adHoc )  "
                        + " GROUP BY CONVERT(DATE, start_time), acknowledgement_flag  " + " UNION ALL  "
                        + " SELECT CONVERT(Date, sch.start_time) as TIME, CASE acknowledgement_flag  "
                        + "         WHEN '0' THEN 'scheduled_unacknowledged'          WHEN null THEN 'scheduled_unacknowledged'  "
                        + "         WHEN '1' THEN 'acknowledged'  END as status, COUNT(*) as COUNT  "
                        + " FROM spotlight_dba.T_SCHEDULED_SUBMISSION sch JOIN spotlight_dba.T_PROCESS p ON sch.process_id = p.id   "
                        + " WHERE sch.submission_id in (select submission_id from spotlight_dba.t_notification_log   "
                        + "                where notification_template_id in (997, 996)  "
                        + "                 AND (sendtime) > (GETDATE() -:days))   "
                        + "                 AND (start_time) > (GETDATE() -:days)  "
                        + "                 AND (acknowledgement_flag <> '0' OR acknowledgement_flag IS NULL)  "
                        + "  AND ( -1 in :processList OR p.id in :processList )  "
                        + "  AND ( -1 in :senderList OR p.sender_id in :senderList )  "
                        + "  AND ( -1 in :receiverList OR p.receiver_id in :receiverList )  "
                        + "  AND ( -1 in :parentList OR p.process_parent_id in :parentList )  "
                        + "  GROUP BY CONVERT(DATE, sch.start_time), sch.acknowledgement_flag) as SourceTable"
                        + " PIVOT ( SUM(COUNT) FOR STATUS IN ([Failed], [Warning],  [in progress], [success], [unacknowledged], [Long Running], [Delayed], [scheduled_unacknowledged])  ) as PivotTable", 
                        nativeQuery = true)
        List<?> findSubmissionStatusCount(@Param("days") Integer days, @Param("processList") int[] processList,
                        @Param("parentList") int[] parentList, @Param("senderList") int[] senderList,
                        @Param("receiverList") int[] receiverList, @Param("bu") String bu,
                        @Param("adHoc") String adHoc);

        @Query(value = "SELECT CONVERT(DATE, s.start_time), p.name, COUNT(*) FROM spotlight_dba.t_submission s"
                        + " JOIN spotlight_dba.t_process p ON s.process_id = p.id" + " WHERE status_id = :status "
                        + " AND (s.start_time) > GETDATE()  - :days"
                        + " AND ( -1 in :processList OR p.id in :processList )"
                        + " AND ( -1 in :senderList OR p.sender_id in :senderList ) "
                        + " AND ( -1 in :receiverList OR p.receiver_id in :receiverList ) "
                        + " AND ( -1 in :parentList OR p.process_parent_id in :parentList ) "
                        + " AND ( :bu = '-1' OR s.bu = :bu) " + " AND ( :adHoc = '-1' OR s.ad_hoc_flag = :adHoc )"
                        + " GROUP BY CONVERT(DATE, s.start_time), p.name" + " ORDER BY 1 desc", nativeQuery = true)
        List<?> findSubmissionStatusPerProcessByDate(@Param("status") Integer status, @Param("days") Integer days,
                        @Param("processList") int[] processList, @Param("parentList") int[] parentList,
                        @Param("senderList") int[] senderList, @Param("receiverList") int[] receiverList,
                        @Param("bu") String bu, @Param("adHoc") String adHoc);

        @Query(value = "SELECT CONVERT(DATE, nl.sendtime), p.name, COUNT(*) "
                        + " FROM spotlight_dba.t_notification_log nl JOIN  spotlight_dba.t_process p ON nl.process_id= p.id "
                        + " LEFT JOIN spotlight_dba.t_submission s ON nl.submission_id= s.id  "
                        + " WHERE  ((notification_template_id in (:status) AND notification_template_id in (997) AND s.end_time is null) OR "
                        + " ((nl.id, nl.scheduled_submission_id) in(select max(nl.id), nl.scheduled_submission_id  "
                        + " from spotlight_dba.t_notification_log nl, spotlight_dba.t_scheduled_submission ss "
                        + " where (nl.sendtime) > (GETDATE()  - :days)  " + " and ss.id = nl.scheduled_submission_id "
                        + " and nl.notification_template_id in (:status) " + " group by nl.scheduled_submission_id))) "
                        + " AND (sendtime) > (GETDATE() -:days)  "
                        + " AND ( -1 in :processList OR p.id in :processList ) "
                        + " AND ( -1 in :senderList OR p.sender_id in :senderList ) "
                        + " AND ( -1 in :receiverList OR p.receiver_id in :receiverList ) "
                        + " AND ( -1 in :parentList OR p.process_parent_id in :parentList ) "
                        + " AND ( :bu = '-1' OR s.bu = :bu)  AND ( :adHoc = '-1' OR s.ad_hoc_flag = :adHoc )  "
                        + " GROUP BY CONVERT(DATE, nl.sendtime), p.name" + " ORDER BY 1 desc", nativeQuery = true)
        List<?> findSubmissionStatusPerProcessByDate2(@Param("status") Integer status, @Param("days") Integer days,
                        @Param("processList") int[] processList, @Param("parentList") int[] parentList,
                        @Param("senderList") int[] senderList, @Param("receiverList") int[] receiverList,
                        @Param("bu") String bu, @Param("adHoc") String adHoc);

        @Query(value = "SELECT  s.id,"
                                + " p.name Process_Name,"
                                + " ps.name First_Step_Failed,"
                                + " ss.start_time Time_Failed,        "
                                + " ssc.end_time Scheduled_End,"
                                + " CASE s.acknowledgement_flag when'0'then'No'when'1'then'Yes'when null THEN'No' END as Acknowledged,"
                                + " ss.end_time Step_End_Time,"
                                + " ps.duration avg_step_duration,"
                                + " s.start_time submission_start_time,"
                                + " ssc.start_time submission_step_start_time,"
                                + " ssc.tolerance scheduled_tolerance,"
                                + " escalated_to.to_emails,"
                                + " s.end_time submission_end_time,"
                                + " s.acknowledgement_note,"
                                + " s.acknowledgement_date,"
                                + " s.acknowledgement_flag "
                        + " FROM"
                                + " spotlight_dba.t_submission as s"
                        + " JOIN spotlight_dba.t_submission_step as ss ON s.id = ss.submission_id"
                        + " LEFT JOIN spotlight_dba.t_scheduled_submission as ssc ON s.id = ssc.submission_id"
                        + " JOIN spotlight_dba.t_process as p ON s.process_id = p.id"
                        + " LEFT JOIN (SELECT id, submission_id, to_emails" 
                                + " FROM spotlight_dba.t_notification_log"
                                + " WHERE notification_template_id in (999)"
                                        + " and (id) in (SELECT max(id)" 
                                                + " FROM spotlight_dba.t_notification_log" 
                                                + " WHERE notification_template_id = 999 "
                                                + " GROUP BY submission_id ) "
                                        + " and (submission_id) in (SELECT submission_id "
                                                                + " FROM spotlight_dba.t_notification_log "
                                                                + " WHERE notification_template_id = 999 "
                                                                + " GROUP BY submission_id ) "
                                + " )  as escalated_to ON s.id = escalated_to.submission_id"
                        + " JOIN spotlight_dba.t_process_step ps ON ps.id = ss.process_step_id "
                        + " JOIN (SELECT id FROM spotlight_dba.t_submission_step "
                                + " WHERE (id) in (SELECT min(id) FROM spotlight_dba.t_submission_step  "
                                                        + " WHERE status_id = 4 "
                                                        + " GROUP BY submission_id)"
                                + " and (submission_id) in (SELECT submission_id FROM spotlight_dba.t_submission_step  "
                                                        + " WHERE status_id = 4 "
                                                        + " GROUP BY submission_id)"
                        + " ) first_failed ON ss.id = first_failed.id "
                        + " WHERE   s.status_id = 4          "
                                + " and (s.start_time) > (GETDATE()  - :days) "
                                + " AND ( -1 in :processList OR p.id in :processList )  "
                                + " AND ( -1 in :senderList OR p.sender_id in :senderList ) "
                                + " AND ( -1 in :receiverList OR p.receiver_id in :receiverList ) "
                                + " AND ( -1 in :parentList OR p.process_parent_id in :parentList ) "
                                + " AND ( :bu = '-1' OR s.bu = :bu)    AND ( :adHoc = '-1' OR s.ad_hoc_flag = :adHoc ) "
                        + " order by id desc, p.name, s.start_time desc ", nativeQuery = true)
        List<?> findSubmissionsFailed(@Param("days") Integer days, @Param("processList") int[] processList,
                        @Param("parentList") int[] parentList, @Param("senderList") int[] senderList,
                        @Param("receiverList") int[] receiverList, @Param("bu") String bu,
                        @Param("adHoc") String adHoc);

        @Query(value = "select s.id, p.name process_name, ps.name Current_step, ss.start_time step_start_time,  " +
			" case when ss.end_time is null then GETDATE()  else ss.end_time end as step_end_time , " + 
                        " ps.duration avg_step_duration, s.start_time submission_start_time, " + 
                        " ssc.start_time scheduled_start_time, ssc.end_time scheduled_end_time, " + 
                        " ssc.tolerance scheduled_tolerance, GETDATE()  today, p.id process_id  "+
                        " from   " +
                        "    spotlight_dba.t_process p, " +
                        "    spotlight_dba.t_submission_step ss, " +
                        "    spotlight_dba.t_process_step ps, " +    
                        "    (select max(st.id) stp, s.id sub from spotlight_dba.t_submission s, spotlight_dba.t_submission_step st " +
                        "                where s.id = st.submission_id  " +
                        "                and (s.start_time) > (GETDATE()  - :days)  " +
                        "                and s.status_id in (1,6)  group by s.id     )  stepsbysub, " +
                        "    spotlight_dba.t_submission s " +
                        "    LEFT OUTER JOIN spotlight_dba.t_scheduled_submission ssc " +
                        "        ON s.id=ssc.submission_id " +
                        "     where stepsbysub.stp = ss.id and stepsbysub.sub = s.id " +
                        "     and s.id=ss.submission_id " +
                        "     and s.process_id= p.id and ps.id=ss.process_step_id and s.status_id in (1,6) " +
                        "     and ps.name <> 'end'    and (s.start_time) > (GETDATE()  - :days) " +
                        "     AND ( -1 in :processList OR p.id in :processList )  " +
                        "     AND ( -1 in :senderList OR p.sender_id in :senderList ) " +  
                        "     AND ( -1 in :receiverList OR p.receiver_id in :receiverList )  " +
                        "     AND ( -1 in :parentList OR p.process_parent_id in :parentList )  " +
                        "     AND ( :bu = '-1' OR s.bu = :bu)   " +
                        "     AND ( :adHoc = '-1' OR s.ad_hoc_flag = :adHoc )  " +
                        " order by s.start_time desc ", nativeQuery = true)
        List<?> findSubmissionsInProgress(@Param("days") Integer days, @Param("processList") int[] processList,
                        @Param("parentList") int[] parentList, @Param("senderList") int[] senderList,
                        @Param("receiverList") int[] receiverList, @Param("bu") String bu,
                        @Param("adHoc") String adHoc);

        @Query(value = "select ss.id,p.name Process_Name, ss.start_time Scheduled_Start, "
                        + " ss.acknowledgement_date Acknowledged_On, nl.to_emails Escalated_To "
                        + " from spotlight_dba.t_notification_log nl, spotlight_dba.t_scheduled_submission ss, spotlight_dba.t_process p "
                        + " where "
                        + " (nl.id) in "
                                + " (select max(id) " 
                                + " from spotlight_dba.t_notification_log "
                                + " where (sendtime) > (GETDATE()  - :days) " 
                                + " and notification_template_id in (998) "
                                + " and submission_id is null " 
                                + " group by scheduled_submission_id) "
                        + " AND (nl.scheduled_submission_id) in "
                                + " (select scheduled_submission_id " 
                                + " from spotlight_dba.t_notification_log "
                                + " where (sendtime) > (GETDATE()  - :days) " 
                                + " AND notification_template_id in (998) "
                                + " AND submission_id is null " 
                                + " group by scheduled_submission_id) "
                        + " AND notification_template_id=998 " 
                        + " AND ss.id= nl.scheduled_submission_id "
                        + " AND ss.process_id=p.id " 
                        + " AND (ss.start_time) > (GETDATE()  - :days) "
                        + " AND ( -1 in :processList OR p.id in :processList )  "
                        + " AND ( -1 in :senderList OR p.sender_id in :senderList ) "
                        + " AND ( -1 in :receiverList OR p.receiver_id in :receiverList ) "
                        + " AND ( -1 in :parentList OR p.process_parent_id in :parentList ) "
                        + " order by ss.start_time desc ", nativeQuery = true)
        List<?> findSubmissionsDelayed(@Param("days") Integer days, @Param("processList") int[] processList,
                        @Param("parentList") int[] parentList, @Param("senderList") int[] senderList,
                        @Param("receiverList") int[] receiverList);

        @Query(value = "SELECT s.id,"
                        + " p.name process_name,"
                        + " ps.name Current_step ,"
                        + " s.alt_id alt_id,"
                        + " ss.start_time step_start_time,"
                        + " ss.end_time step_end_time ,"
                        + " sts.name step_status,"
                        + " s.start_time submission_start_time,"
                        + " s.end_time submission_end_time,"
                        + " GETDATE()  today,"
                        + " s.ad_hoc_flag  "
                        + " FROM spotlight_dba.t_submission s"
                        + " JOIN spotlight_dba.t_submission_step ss ON s.id = ss.submission_id"
                        + " JOIN spotlight_dba.t_process p ON s.process_id = p.id "
                        + " JOIN spotlight_dba.t_process_step ps ON ps.id = ss.process_step_id "
                        + " JOIN spotlight_dba.t_status sts  ON ss.status_id = sts.id"
                        + " WHERE ss.id in (SELECT max(st.id) "
                                        + " FROM spotlight_dba.t_submission s"
                                        + " JOIN spotlight_dba.t_submission_step st ON s.id = st.submission_id     "
                                        + " WHERE (s.start_time) > (GETDATE()  - :days)   "
                                        + " AND s.status_id IN (3, 4) group by s.id)"
                        + " AND s.id in (SELECT s.id "
                                        + " FROM spotlight_dba.t_submission s"
                                        + " JOIN spotlight_dba.t_submission_step st ON s.id = st.submission_id     "
                                        + " WHERE (s.start_time) > (GETDATE()  - :days)   "
                                        + " AND s.status_id IN (3, 4) group by s.id)"
                        + " AND s.status_id=3  "
                        + " AND (s.start_time) > (GETDATE()  - :days)  "
                        + " AND ( -1 in :processList OR p.id in :processList )  "
                        + " AND ( -1 in :senderList OR p.sender_id in :senderList )  "
                        + " AND ( -1 in :receiverList OR p.receiver_id in :receiverList )   "
                        + " AND ( -1 in :parentList OR p.process_parent_id in :parentList )   "
                        + " AND ( :bu = '-1' OR s.bu = :bu)   AND ( :adHoc = '-1' OR s.ad_hoc_flag = :adHoc ) "
                        + " order by s.start_time desc ", nativeQuery = true)
        List<?> findSubmissionsWarning(@Param("days") Integer days, @Param("processList") int[] processList,
                        @Param("parentList") int[] parentList, @Param("senderList") int[] senderList,
                        @Param("receiverList") int[] receiverList, @Param("bu") String bu,
                        @Param("adHoc") String adHoc);

        @Query(value =  "SELECT s.id,"
                        + " p.name process_name,"
                        + " ps.name Current_step ,"
                        + " s.alt_id alt_id,"
                        + " ss.start_time step_start_time,"
                        + " ss.end_time step_end_time ,"
                        + " sts.name step_status,"
                        + " s.start_time submission_start_time,"
                        + " s.end_time submission_end_time,"
                        + " GETDATE()  today,"
                        + " s.ad_hoc_flag  "
                        + " FROM spotlight_dba.t_submission s"
                        + " JOIN spotlight_dba.t_submission_step ss ON s.id = ss.submission_id"
                        + " JOIN spotlight_dba.t_process p ON s.process_id = p.id "
                        + " JOIN spotlight_dba.t_process_step ps ON ps.id = ss.process_step_id "
                        + " JOIN spotlight_dba.t_status sts  ON ss.status_id = sts.id"
                        + " WHERE ss.id in (SELECT max(st.id) "
                                        + " FROM spotlight_dba.t_submission s"
                                        + " JOIN spotlight_dba.t_submission_step st ON s.id = st.submission_id     "
                                        + " WHERE (s.start_time) > (GETDATE()  - :days)   "
                                        + " AND s.status_id = 2 group by s.id)"
                        + " AND s.id in (SELECT s.id "
                                        + " FROM spotlight_dba.t_submission s"
                                        + " JOIN spotlight_dba.t_submission_step st ON s.id = st.submission_id     "
                                        + " WHERE (s.start_time) > (GETDATE()  - :days)   "
                                        + " AND s.status_id = 2 group by s.id)"
                        + " AND s.status_id=2  "
                        + " AND (s.start_time) > (GETDATE()  - :days)  "
                        + " AND ( -1 in :processList OR p.id in :processList )  "
                        + " AND ( -1 in :senderList OR p.sender_id in :senderList )  "
                        + " AND ( -1 in :receiverList OR p.receiver_id in :receiverList )   "
                        + " AND ( -1 in :parentList OR p.process_parent_id in :parentList )   "
                        + " AND ( :bu = '-1' OR s.bu = :bu)   AND ( :adHoc = '-1' OR s.ad_hoc_flag = :adHoc ) "
                        + " order by s.start_time desc ", nativeQuery = true)
        List<?> findSubmissionsSuccess(@Param("days") Integer days, @Param("processList") int[] processList,
                        @Param("parentList") int[] parentList, @Param("senderList") int[] senderList,
                        @Param("receiverList") int[] receiverList, @Param("bu") String bu,
                        @Param("adHoc") String adHoc);

        @Query(value = "select ss.id,ss.submission_id,p.name Process_Name, p.id Process_Id, ss.start_time Scheduled_Start, ss.end_time Scheduled_End,"
                        + " ss.schedule_def_id, ss.acknowledgement_date Acknowledged_On, nl.to_emails Escalated_To "
                        + " from spotlight_dba.t_notification_log nl, spotlight_dba.t_scheduled_submission ss, spotlight_dba.t_process p "
                        + " where "
                        + " (nl.id) in "
                                + " (select max(id) " 
                                + " from spotlight_dba.t_notification_log "
                                + " where (sendtime) > (GETDATE()  - :days) " 
                                + " and notification_template_id in (998) "
                                + " and submission_id is null " 
                                + " group by scheduled_submission_id) "
                        + " AND (nl.scheduled_submission_id) in "
                                + " (select scheduled_submission_id " 
                                + " from spotlight_dba.t_notification_log "
                                + " where (sendtime) > (GETDATE()  - :days) " 
                                + " and notification_template_id in (998) "
                                + " and submission_id is null " 
                                + " group by scheduled_submission_id) "
                        + " AND notification_template_id=998 " 
                        + " and ss.id= nl.scheduled_submission_id "
                        + " AND ss.process_id=p.id " 
                        + " and (ss.start_time) > (GETDATE()  - :days) "
                        + " order by ss.start_time desc ", nativeQuery = true)
        List<?> findSubmissionsDelayedWithId(@Param("days") Integer days);

        @Query(value = "SELECT p.name, subs.*, ISNULL(c.runs,0) runs, ISNULL(nextrun.start_time, '') next_run, lastruns.* "
                                +" FROM                 "
                                        +" (SELECT * FROM "
                                        +" (SELECT status_id as status, last5.process_id as process, rank as ranko "
                                                +" from (SELECT process_id, start_time, status_id, "
                                                +" ROW_NUMBER() OVER (PARTITION BY process_id ORDER BY start_time DESC) rank "
                                                +" FROM spotlight_dba.t_submission  "
                                                +" where start_time > GETDATE()  - 30 ) last5 "
                                        +" where rank <= 5) as SourceTable"
                                        +" PIVOT ( max(status) for ranko in ([1],[2],[3],[4],[5])) as PivotTable) lastruns,                  "
                                        +" spotlight_dba.t_system ss, "
                                        +" spotlight_dba.t_system rs,"
                                        +" spotlight_dba.t_process p"
                                +" JOIN (SELECT ss.process_id,  "
                                        +" ss.start_time actual_start, "
                                        +" ss.end_time actual_end,  "
                                        +" ss.status_id, "
                                        +" sc.start_time scheduled_start,  "
                                        +" sc.end_time scheduled_end, "
                                        +" ROW_NUMBER() OVER (PARTITION BY ss.process_id ORDER BY ss.start_time desc) rank "
                                        +" FROM spotlight_dba.t_submission ss"
                                        +" LEFT JOIN spotlight_dba.t_scheduled_submission sc ON ss.id = sc.submission_id  "
                                        +" WHERE ss.start_time > GETDATE()  - 30) subs ON p.id = subs.process_id "
                                +" LEFT JOIN (SELECT count(*) runs, process_id "
                                        +" FROM spotlight_dba.t_scheduled_submission "
                                        +" where start_time between GETDATE()  "
                                        +" and (GETDATE()  + 8) "
                                        +" group by process_id) c ON subs.process_id = c.process_id"
                                +" LEFT JOIN (SELECT * FROM ("
                                +" SELECT start_time, process_id, "
                                        +" ROW_NUMBER() OVER (PARTITION BY process_id ORDER BY start_time) rank "
                                        +" FROM spotlight_dba.t_scheduled_submission "
                                        +" WHERE start_time between GETDATE()  and (GETDATE()  + 30)) n "
                                        +" WHERE n.rank = 1 ) nextrun ON subs.process_id = nextrun.process_id"
                                +" where "
                                +" (p.sender_id = ss.id)  "
                                +" and (p.receiver_id = rs.id) "
                                +" and (:systemId = '-1' or ss.id = :systemId or rs.id = :systemId)  "
                                +" and subs.rank <= 5         "
                                +" and lastruns.process = subs.process_id "
                                +" ORDER BY p.name, subs.rank ", nativeQuery = true)
        List<?> findSubmissionsCurrentStatus(@Param("systemId") Long systemId);
}
