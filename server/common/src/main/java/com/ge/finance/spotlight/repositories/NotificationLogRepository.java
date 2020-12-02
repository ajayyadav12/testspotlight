package com.ge.finance.spotlight.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

import com.ge.finance.spotlight.models.NotificationLog;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    @Override
    @Query
    List<NotificationLog> findAll();

    List<NotificationLog> findBySubmissionIdAndProcessStepIdIsNull(Long submissionId);

    List<NotificationLog> findBySubmissionIdAndProcessStepId(Long submissionId, Long processStepId);

    List<NotificationLog> findByScheduledSubmissionIdAndNotificationTemplateAndProcessIdIsNotNull(Long submissionId,
            Long templateId);

    boolean existsBySubmissionIdAndNotificationTemplate(Long submissionId, Long notificationTemplate);
}
