package com.ge.finance.spotlight.controllers;

import com.ge.finance.spotlight.dto.NotificationsDTO;
import com.ge.finance.spotlight.exceptions.NotFoundException;
import com.ge.finance.spotlight.models.NotificationTemplate;
import com.ge.finance.spotlight.models.UserNotfication;
import com.ge.finance.spotlight.repositories.NotificationTemplateRepository;
import com.ge.finance.spotlight.repositories.UserNotificationRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.ge.finance.spotlight.services.NotificationDispatcher;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@RestController
@RequestMapping("/v1/notification-templates")
public class NotificationTemplateController {

    private NotificationTemplateRepository notificationTemplateRepository;
    private UserNotificationRepository userNotificationRepository;
    private final NotificationDispatcher dispatcher;
    private static final String unReadStatus = "UnRead";
    private static final String readStatus = "read";

    public NotificationTemplateController(NotificationTemplateRepository notificationTemplateRepository,
            UserNotificationRepository userNotificationRepository, NotificationDispatcher dispatcher) {
        this.notificationTemplateRepository = notificationTemplateRepository;
        this.userNotificationRepository = userNotificationRepository;
        this.dispatcher = dispatcher;
    }

    @GetMapping("/")
    List<NotificationTemplate> index() {
        return notificationTemplateRepository.findAll();
    }

    @PostMapping("/")
    @PreAuthorize("hasAuthority('admin')")
    NotificationTemplate create(@RequestBody NotificationTemplate notificationTemplate) {
        return notificationTemplateRepository.save(notificationTemplate);
    }

    @GetMapping("/{notificationTemplateId}")
    NotificationTemplate get(@PathVariable(name = "notificationTemplateId") Long notificationTemplateId) {
        return notificationTemplateRepository.findById(notificationTemplateId).orElseThrow(NotFoundException::new);
    }

    @PutMapping("/{notificationTemplateId}")
    @PreAuthorize("hasAuthority('admin')")
    NotificationTemplate update(@PathVariable(name = "notificationTemplateId") Long notificationTemplateId,
            @RequestBody NotificationTemplate notificationTemplate) {
        if (notificationTemplateRepository.existsById(notificationTemplateId)) {
            notificationTemplate.setId(notificationTemplateId);
            return notificationTemplateRepository.save(notificationTemplate);
        } else {
            throw new NotFoundException();
        }
    }

    @DeleteMapping("/{notificationTemplateId}")
    @PreAuthorize("hasAuthority('admin')")
    NotificationTemplate delete(@PathVariable(name = "notificationTemplateId") Long notificationTemplateId) {
        NotificationTemplate notificationTemplate = notificationTemplateRepository.findById(notificationTemplateId)
                .orElseThrow(NotFoundException::new);
        notificationTemplateRepository.deleteById(notificationTemplateId);
        return notificationTemplate;
    }

    @GetMapping("/getUserNotifications")
    NotificationsDTO getUserNotifications(Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        Date notificationDays = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.add(Calendar.DATE, -7);
        notificationDays = calendar.getTime();
        return new NotificationsDTO(userNotificationRepository
                .findAllBySsoAndStatusAndStartTimeGreaterThanOrderByStartTimeDesc(sso, unReadStatus, notificationDays));

    }

    @PutMapping("/updateUserNotificationStatus/{uniqueId}")
    public void updateUserNotificationStatus(@PathVariable(name = "uniqueId") Long uniqueId,
            Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        UserNotfication userNotification = userNotificationRepository.findBySsoAndScheduleId(sso, uniqueId);
        if (userNotification != null) {
            userNotification.setStatus(readStatus);
            userNotificationRepository.save(userNotification);
        }
    }

    @MessageMapping("/start")
    public void start(StompHeaderAccessor stompHeaderAccessor) {
        if (null != stompHeaderAccessor.getReceipt() && !stompHeaderAccessor.getReceipt().equals("")) {
            dispatcher.add(stompHeaderAccessor.getSessionId(), stompHeaderAccessor.getReceipt());
            dispatcher.dispatchNotification();
        }
    }

}
