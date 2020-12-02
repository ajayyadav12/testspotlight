package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByProcessId(@Param("processId") Long processId);

    List<Notification> findByProcessIdAndStatusId(Long processId, Long statusId);

    List<Notification> findByProcessStepIdAndStatusId(Long processStepId, Long statusId);

    List<Notification> findByProcessIdAndProcessStepIdIsNullAndStatusIdIsNull(Long processId);

    List<Notification> findByProcessIdAndProcessStepIdIsNullAndStatusIdIsNullAndEnableTextMessagingIsNotNull(
            Long processId);

    List<Notification> findByProcessStepIdAndStatusIdAndEnableTextMessagingIsNotNull(Long processStepId, Long statusId);

    List<Notification> findByProcessIdAndStatusIdAndEnableTextMessagingIsNotNull(Long processId, Long statusId);
        
}
