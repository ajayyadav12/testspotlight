package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

        List<Notification> findAllById(Long NotificationId);

        List<Notification> findByProcessId(@Param("processId") Long processId);

        @Query("select n from Notification n join n.userMobiles um join um.user u where n.processId = :processId and u.id = :userId")
        Collection<Notification> findByProcessIdAndUserId(@Param("processId") Long processId, @Param("userId") Long userId);

        Optional<Notification> findFirstByProcessIdAndStatusId(Long processId, Long statusId);

        Optional<Notification> findFirstByProcessStepIdAndStatusId(Long processStepId, Long statusId);

        Optional<Notification> findFirstByProcessIdAndSubmissionType(Long processId, String submissionType);

        Optional<Notification> findFirstByProcessIdAndSubmissionTypeAndEnableTextMessagingIsNotNull(
                        Long processId, String submissionType);

        Optional<Notification> findByProcessIdAndEscalationTypeAndProcessStepIdIsNullAndStatusIdIsNullAndEnableTextMessagingIsNotNull(
                        Long processId, String escalationType);

        Optional<Notification> findFirstByProcessIdAndSubmissionTypeAndProcessStepIdIsNullAndStatusIdIsNullAndEnableTextMessagingIsNotNull(
                        Long processId, String submissionType);

        Optional<Notification> findFirstByProcessIdAndSubmissionTypeAndProcessStepIdIsNullAndStatusIdIsNull(Long processId,
                        String submissionType);

        Optional<Notification> findFirstByProcessStepIdAndStatusIdAndEnableTextMessagingIsNotNull(Long processStepId,
                        Long statusId);

        Optional<Notification> findFirstByProcessIdAndStatusIdAndEnableTextMessagingIsNotNull(Long processId, Long statusId);

        @Query("select n from Notification n join n.userMobiles um join um.user u where n.id = :id and u.id = :userId")
        Optional<Notification> findFirstByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

}
