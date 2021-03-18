package com.ge.finance.spotlight.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

import com.ge.finance.spotlight.models.NotificationLog;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    @Override
    @Query
    List<NotificationLog> findAll();

    List<NotificationLog> findByProcessId(Long processId);

    boolean existsBySubmissionIdAndProcessStepIdIsNull(Long submissionId);

    boolean existsBySubmissionIdAndProcessStepId(Long submissionId, Long processStepId);

    boolean existsByScheduledSubmissionIdAndNotificationTemplateAndProcessIdIsNotNull(Long submissionId,
            Long templateId);

    boolean existsBySubmissionIdAndNotificationTemplate(Long submissionId, Long notificationTemplate);
}
