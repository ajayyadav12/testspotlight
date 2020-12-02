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

        List<Submission> findByProcessIdAndEndTimeIsNullOrderByStartTimeDesc(Long processId);

        List<Submission> findByStartTimeIsBetweenOrderByStartTimeAsc(Date start, Date end);

        List<Submission> findByProcessIdAndStartTimeIsBetweenOrderByStartTimeAsc(Long processId, Date from, Date to);

        List<Submission> findByParentOrderByStartTimeAsc(Long parentId);

        List<Submission> findByStatusIdAndAcknowledgementFlagAndAckFailedEmailStatusIsNot(Long statusId,
                        boolean acknowledgementFlag, Character ackFailedEmailStatus);

        Submission findFirstByProcessIdAndStatusIdAndAckFailedEmailStatusIsNotAndAcknowledgementFlagTrueOrderByIdDesc(
                        Long processId, Long statusId, Character ackFailedEmailStatus);

        Submission findFirstByProcessIdAndStatusIdIsNotAndIdGreaterThan(Long processId, Long statusId, Long id);

        Optional<Submission> findFirstByProcessIdAndAltIdIgnoreCase(Long processId, String altId);

        List<Submission> findByEndTimeIsNullAndStartTimeGreaterThanOrderByStartTimeDesc(Date date);

        Optional<Submission> findFirstByProcessIdAndIdIsNotOrderByStartTimeDesc(Long processId, Long id);

        @Query(value = "SELECT TIME, NVL(FAILED_SUM_COUNT, 0), NVL(WARNING_SUM_COUNT, 0), NVL(LONG_RUNNING_SUM_COUNT, 0), NVL(DELAYED_SUM_COUNT, 0), NVL(SUCCESS_SUM_COUNT,0), NVL(unacknowledged_SUM_COUNT,0) , NVL(IN_PROGRESS_SUM_COUNT,0), NVL(scheduled_unack_SUM_COUNT,0)  FROM "
                        + "  (SELECT trunc(start_time) as TIME, CASE status_id  "
                        + "    WHEN 3 THEN 'Warning' WHEN 4 THEN 'Failed'  "
                        + "    WHEN 1 THEN 'in progress' WHEN 2 THEN 'success' END as STATUS, COUNT (*) as COUNT  "
                        + "    FROM t_submission s JOIN t_process p ON s.process_id = p.id  "
                        + "    WHERE status_id in (3, 4, 1, 2) AND trunc(start_time) > (SYSDATE-:days)  "
                        + "    AND ( -1 in :processList OR p.id in :processList )  "
                        + "    AND ( -1 in :senderList OR p.sender_id in :senderList )  "
                        + "    AND ( -1 in :receiverList OR p.receiver_id in :receiverList )  "
                        + "    AND ( -1 in :parentList OR p.process_parent_id in :parentList )  "
                        + "    AND ( :bu = '-1' OR s.bu = :bu)  AND ( :adHoc = '-1' OR s.ad_hoc_flag = :adHoc )  "
                        + "    GROUP BY trunc(start_time), status_id  UNION ALL  "
                        + "  SELECT trunc(sendtime) as TIME, CASE notification_template_id WHEN 998 THEN 'Delayed' WHEN 3 THEN 'Delayed' END as STATUS, COUNT(*) as COUNT  "
                        + "   FROM t_scheduled_submission ss, t_notification_log nl JOIN t_process p ON nl.process_id = p.id  "
                        + "   LEFT JOIN t_submission s ON nl.submission_id = s.id  "
                        + "   WHERE (nl.id, nl.scheduled_submission_id) in  "
                        + "          (select max(id), scheduled_submission_id  "
                        + "             from t_notification_log  "
                        + "             where trunc(sendtime) > (sysdate - :days)  "
                        + "             and notification_template_id in (998)  "
                        + "             and submission_id is null  "
                        + "             group by scheduled_submission_id)  "
                        + "         AND ss.id= nl.scheduled_submission_id  "
                        + "         AND notification_template_id in (998)  "
                        + "         AND trunc(sendtime) > (SYSDATE-:days)  "
                        + "         AND ( -1 in :processList OR p.id in :processList )  "
                        + "         AND ( -1 in :senderList OR p.sender_id in :senderList )  "
                        + "         AND ( -1 in :receiverList OR p.receiver_id in :receiverList )  "
                        + "         AND ( -1 in :parentList OR p.process_parent_id in :parentList )  "
                        + "         AND ( :bu = '-1' OR s.bu = :bu)  AND ( :adHoc = '-1' OR s.ad_hoc_flag = :adHoc )  "
                        + " GROUP BY trunc(sendtime), notification_template_id " + "     UNION ALL  "
                        + "   SELECT trunc(sendtime) as TIME, CASE notification_template_id WHEN 997 THEN 'Long Running' END as STATUS, COUNT(*) as COUNT  "
                        + "    FROM t_notification_log nl JOIN t_process p ON nl.process_id = p.id  "
                        + "    LEFT JOIN t_submission s ON nl.submission_id = s.id  "
                        + "    WHERE notification_template_id in (997)  "
                        + "          AND trunc(sendtime) > (SYSDATE-:days) " + "          AND s.end_time is null "
                        + "          AND ( -1 in :processList OR p.id in :processList )  "
                        + "          AND ( -1 in :senderList OR p.sender_id in :senderList )  "
                        + "          AND ( -1 in :receiverList OR p.receiver_id in :receiverList )  "
                        + "          AND ( -1 in :parentList OR p.process_parent_id in :parentList ) "
                        + "          AND ( :bu = '-1' OR s.bu = :bu)  AND ( :adHoc = '-1' OR s.ad_hoc_flag = :adHoc )   "
                        + "  GROUP BY trunc(sendtime), notification_template_id    " + " UNION ALL  "
                        + " SELECT trunc(start_time) as TIME, CASE acknowledgement_flag  "
                        + "          WHEN '0' THEN 'unacknowledged'          WHEN null THEN 'unacknowledged'  "
                        + "          WHEN '1' THEN 'acknowledged'  END as status, COUNT(*) as COUNT  "
                        + "   From T_SUBMISSION s JOIN t_process p ON s.process_id = p.id  "
                        + "   WHERE status_id in (4) AND acknowledgement_flag <> 1  "
                        + "         AND trunc(start_time) > (SYSDATE-:days)  "
                        + "         AND ( -1 in :processList OR p.id in :processList )  "
                        + "         AND ( -1 in :senderList OR p.sender_id in :senderList )  "
                        + "         AND ( -1 in :receiverList OR p.receiver_id in :receiverList )  "
                        + "         AND ( -1 in :parentList OR p.process_parent_id in :parentList )  "
                        + "         AND ( :bu = '-1' OR s.bu = :bu) AND ( :adHoc = '-1' OR s.ad_hoc_flag = :adHoc )  "
                        + " GROUP BY trunc(start_time), acknowledgement_flag  " + " UNION ALL  "
                        + " SELECT trunc(sch.start_time) as TIME, CASE acknowledgement_flag  "
                        + "         WHEN '0' THEN 'scheduled_unacknowledged'          WHEN null THEN 'scheduled_unacknowledged'  "
                        + "         WHEN '1' THEN 'acknowledged'  END as status, COUNT(*) as COUNT  "
                        + " FROM T_SCHEDULED_SUBMISSION sch JOIN T_PROCESS p ON sch.process_id = p.id   "
                        + " WHERE sch.submission_id in (select submission_id from t_notification_log   "
                        + "                where notification_template_id in (997, 996)  "
                        + "                 AND trunc(sendtime) > (SYSDATE-:days))   "
                        + "                 AND trunc(start_time) > (SYSDATE-:days)  "
                        + "                 AND (acknowledgement_flag <> '0' OR acknowledgement_flag IS NULL)  "
                        + "  AND ( -1 in :processList OR p.id in :processList )  "
                        + "  AND ( -1 in :senderList OR p.sender_id in :senderList )  "
                        + "  AND ( -1 in :receiverList OR p.receiver_id in :receiverList )  "
                        + "  AND ( -1 in :parentList OR p.process_parent_id in :parentList )  "
                        + "  GROUP BY trunc(sch.start_time), sch.acknowledgement_flag) PIVOT (  "
                        + "  SUM(COUNT) AS sum_count FOR (STATUS) IN ('Failed' as Failed, 'Warning' as Warning,  "
                        + "  'in progress' as In_Progress, 'success' as Success, 'unacknowledged' as Unacknowledged,  "
                        + "  'Long Running' as Long_Running, 'Delayed' as Delayed, 'scheduled_unacknowledged' as scheduled_unack)  "
                        + "  ) ORDER BY 1 DESC", nativeQuery = true)
        List<?> findSubmissionStatusCount(@Param("days") Integer days, @Param("processList") int[] processList,
                        @Param("parentList") int[] parentList, @Param("senderList") int[] senderList,
                        @Param("receiverList") int[] receiverList, @Param("bu") String bu,
                        @Param("adHoc") String adHoc);

        @Query(value = "SELECT trunc(s.start_time), p.name, COUNT(*) FROM t_submission s"
                        + " JOIN t_process p ON s.process_id = p.id" + " WHERE status_id = :status "
                        + " AND trunc(s.start_time) > SYSDATE - :days"
                        + " AND ( -1 in :processList OR p.id in :processList )"
                        + " AND ( -1 in :senderList OR p.sender_id in :senderList ) "
                        + " AND ( -1 in :receiverList OR p.receiver_id in :receiverList ) "
                        + " AND ( -1 in :parentList OR p.process_parent_id in :parentList ) "
                        + " AND ( :bu = '-1' OR s.bu = :bu) " + " AND ( :adHoc = '-1' OR s.ad_hoc_flag = :adHoc )"
                        + " GROUP BY trunc(s.start_time), p.name" + " ORDER BY 1 desc", nativeQuery = true)
        List<?> findSubmissionStatusPerProcessByDate(@Param("status") Integer status, @Param("days") Integer days,
                        @Param("processList") int[] processList, @Param("parentList") int[] parentList,
                        @Param("senderList") int[] senderList, @Param("receiverList") int[] receiverList,
                        @Param("bu") String bu, @Param("adHoc") String adHoc);

        @Query(value = "SELECT trunc(nl.sendtime), p.name, COUNT(*) "
                        + " FROM t_notification_log nl JOIN  t_process p ON nl.process_id= p.id "
                        + " LEFT JOIN t_submission s ON nl.submission_id= s.id  "
                        + " WHERE  ((notification_template_id in (:status) AND notification_template_id in (997) AND s.end_time is null) OR "
                        + " ((nl.id, nl.scheduled_submission_id) in(select max(nl.id), nl.scheduled_submission_id  "
                        + " from t_notification_log nl, t_scheduled_submission ss "
                        + " where trunc(nl.sendtime) > (sysdate - :days)  " + " and ss.id = nl.scheduled_submission_id "
                        + " and nl.notification_template_id in (:status) " + " group by nl.scheduled_submission_id))) "
                        + " AND trunc(sendtime) > (SYSDATE-:days)  "
                        + " AND ( -1 in :processList OR p.id in :processList ) "
                        + " AND ( -1 in :senderList OR p.sender_id in :senderList ) "
                        + " AND ( -1 in :receiverList OR p.receiver_id in :receiverList ) "
                        + " AND ( -1 in :parentList OR p.process_parent_id in :parentList ) "
                        + " AND ( :bu = '-1' OR s.bu = :bu)  AND ( :adHoc = '-1' OR s.ad_hoc_flag = :adHoc )  "
                        + " GROUP BY trunc(nl.sendtime), p.name" + " ORDER BY 1 desc", nativeQuery = true)
        List<?> findSubmissionStatusPerProcessByDate2(@Param("status") Integer status, @Param("days") Integer days,
                        @Param("processList") int[] processList, @Param("parentList") int[] parentList,
                        @Param("senderList") int[] senderList, @Param("receiverList") int[] receiverList,
                        @Param("bu") String bu, @Param("adHoc") String adHoc);

        @Query(value = "select s.id, p.name Process_Name, ps.name First_Step_Failed, ss.start_time Time_Failed, "
                        + " ssc.end_time Scheduled_End, " + " CASE "
                        + " s.acknowledgement_flag when'0'then'No'when'1'then'Yes'when null THEN'No' "
                        + " END as Acknowledged,  " + " ss.end_time Step_End_Time, "
                        + " ps.duration avg_step_duration,  " + " s.start_time submission_start_time,  "
                        + " ssc.start_time submission_step_start_time,  "
                        + " ssc.tolerance scheduled_tolerance, escalated_to.to_emails, s.end_time submission_end_time, "
                        + " s.acknowledgement_note, s.acknowledgement_date, s.acknowledgement_flag from "
                        + " t_submission s, t_process p, " + " t_submission_step ss, t_process_step ps, "
                        + " t_scheduled_submission ssc,(select* " + " from t_submission_step "
                        + " where (id, submission_id) in ( " + " select min(id), submission_id from t_submission_step  "
                        + "                                          where status_id = 4 "
                        + "                                 group by submission_id)) first_failed, "
                        + "                          (select id, submission_id, to_emails from t_notification_log "
                        + "                             where notification_template_id in (999) "
                        + "                             and (id, submission_id) in (select max(id), submission_id from t_notification_log where notification_template_id = 999 "
                        + "                             group by submission_id " + "                             ) "
                        + "                             order by submission_id desc "
                        + "                          )  escalated_to " + "             where s.id = ss.submission_id "
                        + "                          and s.id = ssc.submission_id (+)          and s.process_id = p.id "
                        + "                          and s.id = escalated_to.submission_id (+) "
                        + "                          and ps.id = ss.process_step_id          and s.status_id = 4  "
                        + "                          and ss.id = first_failed.id    "
                        + " and trunc(s.start_time) > (SYSDATE - :days) "
                        + " AND ( -1 in :processList OR p.id in :processList )  "
                        + " AND ( -1 in :senderList OR p.sender_id in :senderList ) "
                        + " AND ( -1 in :receiverList OR p.receiver_id in :receiverList ) "
                        + " AND ( -1 in :parentList OR p.process_parent_id in :parentList ) "
                        + " AND ( :bu = '-1' OR s.bu = :bu)  " + " AND ( :adHoc = '-1' OR s.ad_hoc_flag = :adHoc ) "
                        + " order by id desc, p.name, s.start_time desc ", nativeQuery = true)
        List<?> findSubmissionsFailed(@Param("days") Integer days, @Param("processList") int[] processList,
                        @Param("parentList") int[] parentList, @Param("senderList") int[] senderList,
                        @Param("receiverList") int[] receiverList, @Param("bu") String bu,
                        @Param("adHoc") String adHoc);

        @Query(value = "select s.id, p.name process_name, ps.name Current_step, ss.start_time step_start_time, case when ss.end_time is null then sysdate else ss.end_time end as step_end_time , "
                        + "     ps.duration avg_step_duration, " + "s.start_time submission_start_time, "
                        + "     ssc.start_time scheduled_start_time, " + "ssc.end_time scheduled_end_time, "
                        + "     ssc.tolerance scheduled_tolerance, sysdate today " + "from t_submission s, "
                        + "     t_process p, t_submission_step ss, " + "t_process_step ps, t_scheduled_submission ssc "
                        + "     where (ss.id, s.id) in (select max(st.id), s.id from t_submission s, t_submission_step st "
                        + "                where s.id = st.submission_id "
                        + "                and trunc(s.start_time) > (SYSDATE - :days) "
                        + "                and s.status_id=1 " + "                group by s.id     )"
                        + "     and s.id=ss.submission_id and s.id=ssc.submission_id(+) "
                        + "     and s.process_id= p.id and ps.id=ss.process_step_id and s.status_id=1 "
                        // + " and (ss.end_time is null or ps.name like 'start') "
                        + "     and ps.name <> 'end' " + "     and trunc(s.start_time) > (SYSDATE - :days) "
                        + "     AND ( -1 in :processList OR p.id in :processList )  "
                        + "     AND ( -1 in :senderList OR p.sender_id in :senderList )  "
                        + "     AND ( -1 in :receiverList OR p.receiver_id in :receiverList )  "
                        + "     AND ( -1 in :parentList OR p.process_parent_id in :parentList )  "
                        + "     AND ( :bu = '-1' OR s.bu = :bu)   "
                        + "     AND ( :adHoc = '-1' OR s.ad_hoc_flag = :adHoc )  "
                        + " order by s.start_time desc", nativeQuery = true)
        List<?> findSubmissionsInProgress(@Param("days") Integer days, @Param("processList") int[] processList,
                        @Param("parentList") int[] parentList, @Param("senderList") int[] senderList,
                        @Param("receiverList") int[] receiverList, @Param("bu") String bu,
                        @Param("adHoc") String adHoc);

        @Query(value = "select ss.id,p.name Process_Name, ss.start_time Scheduled_Start, "
                        + " ss.acknowledgement_date Acknowledged_On, nl.to_emails Escalated_To "
                        + " from t_notification_log nl, t_scheduled_submission ss, t_process p "
                        + " where (nl.id, nl.scheduled_submission_id) in "
                        + " (select max(id), scheduled_submission_id " + " from t_notification_log "
                        + " where trunc(sendtime) > (sysdate - :days) " + " and notification_template_id in (998) "
                        + " and submission_id is null " + " group by scheduled_submission_id) "
                        + " AND notification_template_id=998 " + "and ss.id= nl.scheduled_submission_id "
                        + " AND ss.process_id=p.id " + " and trunc(ss.start_time) > (SYSDATE - :days) "
                        + " AND ( -1 in :processList OR p.id in :processList )  "
                        + " AND ( -1 in :senderList OR p.sender_id in :senderList ) "
                        + " AND ( -1 in :receiverList OR p.receiver_id in :receiverList ) "
                        + " AND ( -1 in :parentList OR p.process_parent_id in :parentList ) "
                        + " order by ss.start_time desc ", nativeQuery = true)
        List<?> findSubmissionsDelayed(@Param("days") Integer days, @Param("processList") int[] processList,
                        @Param("parentList") int[] parentList, @Param("senderList") int[] senderList,
                        @Param("receiverList") int[] receiverList);

        @Query(value = "select s.id, p.name process_name, ps.name Current_step, s.alt_id alt_id, ss.start_time step_start_time, "
                        + " ss.end_time step_end_time , sts.name step_status, s.start_time submission_start_time, s.end_time submission_end_time, "
                        + " sysdate  today, s.ad_hoc_flag "
                        + " from t_submission s, t_process p, t_submission_step ss, t_process_step ps, t_status sts "
                        + " where (ss.id, s.id) in (select max(st.id), s.id from t_submission s, t_submission_step st "
                        + " where s.id = st.submission_id " + " and trunc(s.start_time) > (SYSDATE - :days) "
                        + " and s.status_id = 3 and " + " st.status_id in (3, 4) group by s.id)  "
                        + " and s.process_id= p.id and ps.id=ss.process_step_id and s.status_id=3  "
                        + " and ss.status_id = sts.id " + " and trunc(s.start_time) > (SYSDATE - :days) "
                        + " AND ( -1 in :processList OR p.id in :processList ) "
                        + " AND ( -1 in :senderList OR p.sender_id in :senderList )  "
                        + " AND ( -1 in :receiverList OR p.receiver_id in :receiverList )  "
                        + " AND ( -1 in :parentList OR p.process_parent_id in :parentList ) "
                        + " AND ( :bu = '-1' OR s.bu = :bu)   " + " AND ( :adHoc = '-1' OR s.ad_hoc_flag = :adHoc )  "
                        + " order by s.start_time desc ", nativeQuery = true)
        List<?> findSubmissionsWarning(@Param("days") Integer days, @Param("processList") int[] processList,
                        @Param("parentList") int[] parentList, @Param("senderList") int[] senderList,
                        @Param("receiverList") int[] receiverList, @Param("bu") String bu,
                        @Param("adHoc") String adHoc);

        @Query(value = " select s.id, p.name process_name, ps.name Current_step , s.alt_id alt_id, ss.start_time step_start_time, "
                        + " ss.end_time step_end_time , sts.name step_status, s.start_time submission_start_time, s.end_time submission_end_time,  "
                        + " sysdate today, s.ad_hoc_flag  "
                        + " from t_submission s, t_process p, t_submission_step ss, t_process_step ps, t_status sts  "
                        + " where (ss.id, s.id) in (select max(st.id), s.id from t_submission s, t_submission_step st   "
                        + "                         where s.id = st.submission_id        and trunc(s.start_time) > (SYSDATE - :days)   "
                        + "                          and s.status_id = 2 group by s.id)  "
                        + " and s.process_id= p.id and ps.id=ss.process_step_id and s.status_id=2  "
                        + " and ss.status_id = sts.id  " + " and trunc(s.start_time) > (SYSDATE - :days)  "
                        + " AND ( -1 in :processList OR p.id in :processList )  "
                        + " AND ( -1 in :senderList OR p.sender_id in :senderList )  "
                        + " AND ( -1 in :receiverList OR p.receiver_id in :receiverList )   "
                        + " AND ( -1 in :parentList OR p.process_parent_id in :parentList )   "
                        + " AND ( :bu = '-1' OR s.bu = :bu) " + " AND ( :adHoc = '-1' OR s.ad_hoc_flag = :adHoc ) "
                        + " order by s.start_time desc ", nativeQuery = true)
        List<?> findSubmissionsSuccess(@Param("days") Integer days, @Param("processList") int[] processList,
                        @Param("parentList") int[] parentList, @Param("senderList") int[] senderList,
                        @Param("receiverList") int[] receiverList, @Param("bu") String bu,
                        @Param("adHoc") String adHoc);

}
