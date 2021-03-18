package com.ge.finance.spotlight.repositories;

import javax.transaction.Transactional;

import com.ge.finance.spotlight.models.NotificationMobile;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationMobileRepository extends JpaRepository<NotificationMobile, Long> {

    @Transactional
    void deleteByNotificationId(Long notificationId);

    long deleteByIdAndUserId(Long id, Long userId);

    NotificationMobile findByNotificationIdAndUserId(Long id, Long userId);

}
