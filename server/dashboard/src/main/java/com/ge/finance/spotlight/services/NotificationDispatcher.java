package com.ge.finance.spotlight.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.ge.finance.spotlight.repositories.ProcessUserRepository;
import com.ge.finance.spotlight.repositories.ScheduledSubmissionRepository;
import com.ge.finance.spotlight.repositories.SubmissionRepository;
import com.ge.finance.spotlight.repositories.UserNotificationRepository;
import com.ge.finance.spotlight.repositories.UserRepository;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.context.event.EventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Collection;

import com.ge.finance.spotlight.dto.NotificationsDTO;
import com.ge.finance.spotlight.models.ScheduledSubmission;
import com.ge.finance.spotlight.models.Submission;
import com.ge.finance.spotlight.models.User;
import com.ge.finance.spotlight.models.ProcessUser;
import com.ge.finance.spotlight.models.UserNotfication;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Optional;

@Service
@EnableScheduling
public class NotificationDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationDispatcher.class);

    private final SimpMessagingTemplate template;

    private SubmissionRepository submissionRepository;

    private ScheduledSubmissionRepository scheduledSubmissionRepository;
    private static final long MINUTES_1 = 60_000;
    private static final String readStatus = "UnRead";
    private static final String delayed = "Delayed";
    private static final String longRunning = "LongRunning";
    private static final String processType = "ReleaseNotes";
    private Set<String> listeners = new HashSet<>();
    private long sso;
    private String role;
    private UserRepository userRepository;

    private ProcessUserRepository processUserRepository;

    private UserNotificationRepository userNotificationRepository;

    public NotificationDispatcher(SimpMessagingTemplate template, SubmissionRepository submissionRepository,
            ScheduledSubmissionRepository scheduledSubmissionRepository, UserRepository userRepository,
            ProcessUserRepository processUserRepository, UserNotificationRepository userNotificationRepository) {
        this.template = template;
        this.submissionRepository = submissionRepository;
        this.scheduledSubmissionRepository = scheduledSubmissionRepository;
        this.userRepository = userRepository;
        this.processUserRepository = processUserRepository;
        this.userNotificationRepository = userNotificationRepository;
    }

    public void add(String sessionId, String ssoRole) {
        String[] values = ssoRole.split(",");
        this.sso = Long.parseLong(values[0]);
        this.role = values[1];
        listeners.add(sessionId);
    }

    public void remove(String sessionId) {
        listeners.remove(sessionId);
    }

    @Scheduled(fixedDelay = 30000)
    public void dispatchNotification() {

        int days = 7;
        Date notificationDays = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.add(Calendar.DATE, -7);
        notificationDays = calendar.getTime();
        for (String listener : listeners) {

            List<?> delayedList = null;
            List<NotificationsDTO> finalNotificationList = new ArrayList();
            List<NotificationsDTO> longRunSubmissionsList = new ArrayList();
            SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
            headerAccessor.setSessionId(listener);
            headerAccessor.setLeaveMutable(true);

            delayedList = submissionRepository.findSubmissionsDelayedWithId(days);

            longRunSubmissionsList = longRunSubmissionsList();
            finalNotificationList = getFinalDelayedList(delayedList, finalNotificationList);
            finalNotificationList.addAll(longRunSubmissionsList);
            // if (!role.equals("admin")) {
            finalNotificationList = getfinalListBySso(sso, finalNotificationList);
            // }

            if (finalNotificationList != null && finalNotificationList.size() > 0) {
                saveNotificationData(finalNotificationList);
            }

            saveReleaseNotesData();

            List<UserNotfication> listUserNotification = userNotificationRepository
                    .findAllBySsoAndStatusAndStartTimeGreaterThanOrderByStartTimeDesc(sso, readStatus,
                            notificationDays);

            template.convertAndSendToUser(listener, "/notification/item", new NotificationsDTO(listUserNotification),
                    headerAccessor.getMessageHeaders());
        }

    }

    private synchronized void saveNotificationData(List<NotificationsDTO> finalNotificationList) {
        for (NotificationsDTO notification : finalNotificationList) {
            UserNotfication userNotify = new UserNotfication();
            if (!userNotificationRepository.existsBySsoAndScheduleId(sso, notification.getScheduleId())) {
                userNotify.setSso(sso);
                userNotify.setSubmissionId(notification.getSubmissionId());
                userNotify.setProcessName(notification.getProcessName());
                userNotify.setStatus(readStatus);
                userNotify.setProcessType(notification.getProcessType());
                userNotify.setProcessId(notification.getProcessId());
                userNotify.setScheduleId(notification.getScheduleId());
                userNotify.setStartTime(notification.getStartTime());
                userNotify.setEndTime(notification.getEndTime());
                userNotify.setScheduledefId(notification.getScheduleDefID());
                userNotificationRepository.save(userNotify);
            }
        }

    }

    private synchronized void saveReleaseNotesData() {
        // Changes for ReleaseNotes
        long value = 0;
        if (userNotificationRepository.existsBySsoAndProcessTypeAndStatus(value, processType, readStatus)
                && !userNotificationRepository.existsBySsoAndProcessType(sso, processType)) {
            UserNotfication userNotification = new UserNotfication();
            userNotification.setSso(sso);
            userNotification.setProcessType(processType);
            userNotification.setStatus(readStatus);
            userNotification.setStartTime(new Date());
            userNotificationRepository.save(userNotification);
        }
    }

    private List<?> convertObjectToList(Object obj) {
        List<?> list = new ArrayList<>();
        if (obj.getClass().isArray()) {
            list = Arrays.asList((Object[]) obj);
        } else if (obj instanceof Collection) {
            list = new ArrayList<>((Collection<?>) obj);
        }
        return list;
    }

    private List<NotificationsDTO> getFinalDelayedList(List<?> delayedList, List<NotificationsDTO> notificationList) {

        for (Object objDelayed : delayedList) {
            List<?> objectList = convertObjectToList(objDelayed);
            NotificationsDTO notification = new NotificationsDTO();
            notification.setScheduleId(Long.parseLong(objectList.get(0).toString()));

            if (null != objectList.get(1)) {
                notification.setSubmissionId(Integer.parseInt(objectList.get(1).toString()));
            }
            notification.setProcessName(objectList.get(2).toString());
            notification.setProcessId(Long.parseLong(objectList.get(3).toString()));
            notification.setStartTime((Date) objectList.get(4));
            notification.setEndTime((Date) objectList.get(5));
            notification.setScheduleDefID(Long.parseLong(objectList.get(6).toString()));
            notification.setProcessType(delayed);
            notificationList.add(notification);

        }
        return notificationList;
    }

    public List<NotificationsDTO> longRunSubmissionsList() {
        Date now = new Date();
        Date days = new Date();
        List<NotificationsDTO> notificationList = new ArrayList<>();
        Calendar c = new GregorianCalendar();
        c.add(Calendar.DATE, -2);
        days = c.getTime();
        List<Submission> runningSubmissions = submissionRepository
                .findByEndTimeIsNullAndStartTimeGreaterThanOrderByStartTimeDesc(days);

        for (Submission submission : runningSubmissions) {
            NotificationsDTO notification = new NotificationsDTO();
            Optional<ScheduledSubmission> optScheduledSubmission = scheduledSubmissionRepository
                    .findBySubmissionId(submission.getId());

            if (optScheduledSubmission.isPresent()) {
                ScheduledSubmission scheduledSubmission = optScheduledSubmission.get();

                long scheduledDuration = (scheduledSubmission.getEndTime().getTime()
                        - scheduledSubmission.getStartTime().getTime())
                        + (scheduledSubmission.getTolerance() * MINUTES_1);
                long actualDuration = now.getTime() - submission.getStartTime().getTime();

                if (scheduledDuration > 0 && actualDuration > scheduledDuration) {
                    notification.setScheduleId(submission.getId());
                    notification.setSubmissionId(submission.getId().intValue());
                    notification.setProcessName(submission.getProcess().getName());
                    notification.setProcessId(submission.getProcess().getId());
                    notification.setProcessType(longRunning);
                    notification.setStartTime(scheduledSubmission.getStartTime());
                    notification.setEndTime(scheduledSubmission.getEndTime());
                    notification.setScheduleDefID(scheduledSubmission.getScheduleDefinitionId());
                    notificationList.add(notification);
                }
            }

        }
        return notificationList;
    }

    List<NotificationsDTO> getfinalListBySso(Long sso, List<NotificationsDTO> finalNotificationList) {

        List<NotificationsDTO> notificationList = new ArrayList();
        User user = userRepository.findFirstBySso(sso);
        List<Long> processIdList = processUserRepository.findByUserId(user.getId()).stream()
                .map(ProcessUser::getProcessId).collect(Collectors.toList());
        if (!processIdList.isEmpty()) {
            for (long processId : processIdList) {
                for (NotificationsDTO notification : finalNotificationList) {
                    if (processId == notification.getProcessId()) {
                        notificationList.add(notification);
                    }
                }
            }
        }

        return notificationList;
    }

    @EventListener
    public void sessionDisconnectionHandler(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        LOGGER.info("Disconnecting " + sessionId + "!");
        remove(sessionId);
    }

}
