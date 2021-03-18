package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.Notification;
import com.ge.finance.spotlight.models.NotificationMobile;
import com.ge.finance.spotlight.models.Process;
import com.ge.finance.spotlight.models.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(SpringRunner.class)
@DataJpaTest
public class NotificationRepositoryJpaTest {

    @Autowired private NotificationRepository notificationRepository;
    @Autowired private NotificationMobileRepository notificationMobileRepository;
    @Autowired private ProcessRepository processRepository;
    @Autowired private UserRepository userRepository;

    @Test
    public void testFindByProcessIdAndUserId() {
        Process process = new Process();
        process = processRepository.save(process);
        User user = new User();
        user = userRepository.save(user);
        Notification notification = new Notification();
        notification.setProcessId(process.getId());
        notification = notificationRepository.save(notification);
        NotificationMobile notificationMobile = new NotificationMobile();
        notificationMobile.setUser(user);
        notificationMobile.setNotificationId(notification.getId());
        notificationMobileRepository.save(notificationMobile);
        Collection<Notification> notifications = notificationRepository.findByProcessIdAndUserId(process.getId(), user.getId());
        assertEquals(1, notifications.size());
    }

    @Test
    public void testFindFirstByIdAndUserId() {
        Process process = new Process();
        process = processRepository.save(process);
        User user = new User();
        user = userRepository.save(user);
        Notification notification = new Notification();
        notification.setProcessId(process.getId());
        notification = notificationRepository.save(notification);
        NotificationMobile notificationMobile = new NotificationMobile();
        notificationMobile.setUser(user);
        notificationMobile.setNotificationId(notification.getId());
        notificationMobileRepository.save(notificationMobile);
        Optional<Notification> optionalNotification = notificationRepository.findFirstByIdAndUserId(notification.getId(), user.getId());
        assertTrue(optionalNotification.isPresent());
        assertEquals(notification.getId(), optionalNotification.get().getId());
    }

}
