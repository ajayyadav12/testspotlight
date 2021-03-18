package com.ge.finance.spotlight.controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import com.ge.finance.spotlight.dto.AcknowledgeDTO;
import com.ge.finance.spotlight.dto.EmailModel;
import com.ge.finance.spotlight.exceptions.BadRequestException;
import com.ge.finance.spotlight.exceptions.NotFoundException;
import com.ge.finance.spotlight.filters.FilterBuilder;
import com.ge.finance.spotlight.libs.AcknowledgeLib;
import com.ge.finance.spotlight.models.MessageGateway;
import com.ge.finance.spotlight.models.Notification;
import com.ge.finance.spotlight.models.NotificationMobile;
import com.ge.finance.spotlight.models.Process;
import com.ge.finance.spotlight.models.ProcessUser;
import com.ge.finance.spotlight.models.ScheduledSubmission;
import com.ge.finance.spotlight.models.User;
import com.ge.finance.spotlight.repositories.MessageGatewayRepository;
import com.ge.finance.spotlight.repositories.NotificationRepository;
import com.ge.finance.spotlight.repositories.ProcessRepository;
import com.ge.finance.spotlight.repositories.ProcessUserRepository;
import com.ge.finance.spotlight.repositories.ScheduledSubmissionRepository;
import com.ge.finance.spotlight.repositories.UserRepository;
import com.ge.finance.spotlight.security.Constants;
import com.ge.finance.spotlight.services.SpotlightEmailService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/scheduled-submissions")
public class ScheduledSubmissionController {

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private List<String> allowedProcessFilters = Arrays.asList("sender", "receiver", "parentId", "childId");

    private ScheduledSubmissionRepository scheduledSubmissionRepository;
    private NotificationRepository notificationRepository;
    private UserRepository userRepository;
    private ProcessUserRepository processUserRepository;
    private ProcessRepository processRepository;
    private SpotlightEmailService spotlightEmailService;
    private MessageGatewayRepository messageGatewayRepository;
    List<ScheduledSubmission> scheduledSubmissionsList = null;

    private ScheduledSubmissionController(ScheduledSubmissionRepository scheduledSubmissionRepository,
            UserRepository userRepository, ProcessUserRepository processUserRepository,
            NotificationRepository notificationRepository, ProcessRepository processRepository,
            SpotlightEmailService spotlightEmailService, MessageGatewayRepository messageGatewayRepository) {
        this.scheduledSubmissionRepository = scheduledSubmissionRepository;
        this.userRepository = userRepository;
        this.processUserRepository = processUserRepository;
        this.processRepository = processRepository;
        this.notificationRepository = notificationRepository;
        this.spotlightEmailService = spotlightEmailService;
        this.messageGatewayRepository = messageGatewayRepository;
    }

    @GetMapping("/")
    List<ScheduledSubmission> index(@RequestParam String from, @RequestParam String to, Authentication authentication) {
        Date fromDate, toDate;
        try {
            fromDate = formatter.parse(from);
            toDate = formatter.parse(to);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(fromDate);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            fromDate = calendar.getTime();
            calendar.setTime(toDate);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            toDate = calendar.getTime();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException();
        }
        scheduledSubmissionsList = scheduledSubmissionRepository
                .findByStartTimeIsBetweenAndSubmissionIdIsNullOrderByStartTimeAsc(fromDate, toDate);
        if (!scheduledSubmissionsList.isEmpty()) {
            scheduledSubmissionsList.forEach(item -> {
                item.setProcesName(item.getProcess().getName());
                item.setProcesId(item.getProcess().getId());
                item.setProcess(null);
            });
        }
        return scheduledSubmissionsList;
    }

    @GetMapping("/filterSubmissions")
    public List<ScheduledSubmission> filterScheduleSubmission(@RequestParam Map<String, String> filters,
            Authentication authentication) {
        Date fromDate, toDate;
        String processIdListFiltered = null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        fromDate = calendar.getTime();
        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH, 3);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        toDate = calendar.getTime();
        List<Long> processIds = new ArrayList<>();
        Map<String, String> processFilters = filters.entrySet().stream()
                .filter(entry -> allowedProcessFilters.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (processFilters.size() != 0) {
            FilterBuilder<Process> processFilterBuilder = new FilterBuilder<>(processFilters, true);
            processIdListFiltered = processRepository.findAll(processFilterBuilder.build()).stream()
                    .map(p -> Long.toString(p.getId())).collect(Collectors.joining(","));
            processIdListFiltered = processIdListFiltered == "" ? "0" : processIdListFiltered;
            List<String> processlist = Arrays.asList(processIdListFiltered.split(","));
            for (String proList : processlist) {
                processIds.add(Long.parseLong(proList));
            }

        }
        scheduledSubmissionsList = scheduledSubmissionRepository
                .findByStartTimeIsBetweenAndProcessIdIsInAndSubmissionIdIsNullOrderByStartTimeAsc(fromDate, toDate,
                        processIds);
        if (!scheduledSubmissionsList.isEmpty()) {
            scheduledSubmissionsList.forEach(item -> {
                item.setProcesName(item.getProcess().getName());
                item.setProcesId(item.getProcess().getId());
                item.setProcess(null);
            });
        }
        return scheduledSubmissionsList;
    }

    @PostMapping("/{submissionId}/acknowledgement")
    public ScheduledSubmission setAcknowledgementFlag(@PathVariable(value = "submissionId") Long submissionId,
            @RequestBody AcknowledgeDTO acknowledgeDTO, Authentication authentication) {
        // Validate user is the app owner or member of team
        Long sso = (Long) authentication.getPrincipal();
        ScheduledSubmission submission = scheduledSubmissionRepository.findById(submissionId).get();
        if (isProcessUser(sso, submission.getProcess().getId())
                || AcknowledgeLib.IsAllowedToAcknowledge(submission.getProcess(), sso)) {
            submission.setAcknowledgementFlag(true);
            submission.setAcknowledgementDate(new Date());
            submission.setAcknowledgementNote(acknowledgeDTO.getAcknowledgementNote());
            submission.setAcknowledgedBy(acknowledgeDTO.getAcknowledgedBy());
            return scheduledSubmissionRepository.save(submission);
        } else {
            throw new RuntimeException("Only Application Owner and team can acknowledge a failure.");
        }
    }

    @PostMapping("/{submissionId}/disable")
    public ScheduledSubmission setDisable(@PathVariable(value = "submissionId") Long submissionId,
            @RequestBody AcknowledgeDTO disabledRequest, Authentication authentication) {
        // Validate user is the app owner or member of team
        Long sso = (Long) authentication.getPrincipal();
        ScheduledSubmission submission = scheduledSubmissionRepository.findById(submissionId).get();
        if (isProcessUser(sso, submission.getProcess().getId())
                || AcknowledgeLib.IsAllowedToAcknowledge(submission.getProcess(), sso)) {
            submission.setDisabled(true);
            submission.setDisabledNote(disabledRequest.getAcknowledgementNote());
            notificationRepository
                    .findFirstByProcessIdAndSubmissionTypeAndProcessStepIdIsNullAndStatusIdIsNullAndEnableTextMessagingIsNotNull(
                            submission.getProcess().getId(), Constants.DISABLED_SUBMISSION)
                    .ifPresent(not -> {
                        sendEmail(not, submission);
                        sendSMS(not, submission);
                    });
            return scheduledSubmissionRepository.save(submission);
        } else {
            throw new RuntimeException("Only Application Owner and team are allowed to disable.");
        }
    }

    @GetMapping("/{scheduleDefId}/scheduleSubmissionList")
    List<ScheduledSubmission> getScheduleSubmissions(Authentication authentication,
            @PathVariable(value = "scheduleDefId") Long scheduleDefId) {
        Date fromDate, toDate;
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            fromDate = calendar.getTime();

            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_MONTH, 180);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            toDate = calendar.getTime();

            scheduledSubmissionsList = scheduledSubmissionRepository
                    .findByStartTimeIsBetweenAndScheduleDefinitionIdIsOrderByStartTimeAsc(fromDate, toDate,
                            scheduleDefId);
            List<?> delayedSubmissionList = scheduledSubmissionRepository.findDelayedSubmissions(7, scheduleDefId);
            List<ScheduledSubmission> getFinalDelayedList = getDelayedList(delayedSubmissionList);

            if (!getFinalDelayedList.isEmpty()) {
                scheduledSubmissionsList.addAll(getFinalDelayedList);
            }

            if (!scheduledSubmissionsList.isEmpty()) {
                scheduledSubmissionsList.forEach(item -> {
                    item.setProcess(null);
                });
            }

            return scheduledSubmissionsList;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException();
        }
    }

    @GetMapping("/{submissionId}")
    public ScheduledSubmission getSubmission(@PathVariable(value = "submissionId") Long submissionId) {
        return scheduledSubmissionRepository.findBySubmissionId(submissionId).orElseThrow(NotFoundException::new);
    }

    @PostMapping("/{scheduleSubmissionId}/update")
    public ScheduledSubmission update(@PathVariable(value = "scheduleSubmissionId") Long scheduleSubmissionId,
            @RequestParam String from, @RequestParam String to, @RequestParam String notes,
            Authentication authentication) {
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            isoFormat.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
            Date fromDate = isoFormat.parse(from);
            Date toDate = isoFormat.parse(to);

            ScheduledSubmission submission = scheduledSubmissionRepository.findById(scheduleSubmissionId).get();
            submission.setStartTime(fromDate);
            submission.setEndTime(toDate);
            submission.setEditNotes(notes);

            notificationRepository
                    .findFirstByProcessIdAndSubmissionTypeAndProcessStepIdIsNullAndStatusIdIsNullAndEnableTextMessagingIsNotNull(
                            submission.getProcess().getId(), Constants.EDITED_SUBMISSION)
                    .ifPresent(not -> {
                        sendEmail(not, submission);
                        sendSMS(not, submission);
                    });
            return scheduledSubmissionRepository.save(submission);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException();
        }
    }

    private boolean isProcessUser(Long sso, Long processId) {
        User user = userRepository.findFirstBySso(sso);
        List<ProcessUser> processUsers = processUserRepository.findByUserIdAndProcessId(user.getId(), processId);
        return !processUsers.isEmpty();
    }

    private void sendEmail(Notification notification, ScheduledSubmission submission) {

        String emailList = notification.getAdditionalEmails().trim() + " "
                + submission.getProcess().getSupportTeamEmail().trim();
        emailList = emailList.trim();
        if (!emailList.isEmpty()) {
            EmailModel emailModel = new EmailModel(notification.getNotificationTemplateID(), emailList, false);
            emailModel.schedSubmission = submission;
            spotlightEmailService.genericSend(emailModel);
        }

    }

    private void sendSMS(Notification notification, ScheduledSubmission submission) {
        Iterator<NotificationMobile> iNotificationMobile = notification.getUserMobiles().iterator();
        while (iNotificationMobile.hasNext()) {
            NotificationMobile tempNotificationMobile = iNotificationMobile.next();

            User user = tempNotificationMobile.getUser();
            MessageGateway messageGateway = messageGatewayRepository.findById(user.getCarrier().getId())
                    .orElseThrow(NotFoundException::new);
            String smsGateway = user.getPhoneNumber() + messageGateway.getGateway();
            if (!smsGateway.isEmpty()) {
                EmailModel emailModel = new EmailModel(notification.getNotificationTemplateID(), smsGateway, true);
                emailModel.schedSubmission = submission;
                spotlightEmailService.genericSend(emailModel);
            }
        }
    }

    private List<ScheduledSubmission> getDelayedList(List<?> delayedList) {
        List<ScheduledSubmission> getDelayedList = new ArrayList<>();
        for (Object objDelayed : delayedList) {
            List<?> objectList = convertObjectToList(objDelayed);
            ScheduledSubmission scheduledSubmission = new ScheduledSubmission();
            scheduledSubmission.setId(Long.parseLong(objectList.get(0).toString()));
            scheduledSubmission.setStartTime((Date) objectList.get(1));
            scheduledSubmission.setEndTime((Date) objectList.get(2));
            if (objectList.get(3) != null) {
                scheduledSubmission.setAcknowledgementFlag(Boolean.valueOf(objectList.get(3).toString()));
            }
            if (objectList.get(4) != null) {
                scheduledSubmission.setDisabled(Boolean.valueOf(objectList.get(4).toString()));
            }
            scheduledSubmission.setDelayed(true);
            getDelayedList.add(scheduledSubmission);

        }
        return getDelayedList;
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
}
