package com.ge.finance.spotlight.cronjobs;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ge.finance.spotlight.dto.EmailModel;
import com.ge.finance.spotlight.dto.ProcessDTO;
import com.ge.finance.spotlight.exceptions.NotFoundException;
import com.ge.finance.spotlight.libs.GEOneHRConnection;
import com.ge.finance.spotlight.models.AnalyticsReport;
import com.ge.finance.spotlight.models.MessageGateway;
import com.ge.finance.spotlight.models.Notification;
import com.ge.finance.spotlight.models.NotificationMobile;
import com.ge.finance.spotlight.models.NotificationTemplate;
import com.ge.finance.spotlight.models.Process;
import com.ge.finance.spotlight.models.ProcessStep;
import com.ge.finance.spotlight.models.ProcessUser;
import com.ge.finance.spotlight.models.ScheduleDefinition;
import com.ge.finance.spotlight.models.ScheduleReport;
import com.ge.finance.spotlight.models.ScheduledSubmission;
import com.ge.finance.spotlight.models.Status;
import com.ge.finance.spotlight.models.Submission;
import com.ge.finance.spotlight.models.SubmissionRequest;
import com.ge.finance.spotlight.models.SubmissionStep;
import com.ge.finance.spotlight.models.User;
import com.ge.finance.spotlight.repositories.AnalyticsReportRepository;
import com.ge.finance.spotlight.repositories.MessageGatewayRepository;
import com.ge.finance.spotlight.repositories.NotificationLogRepository;
import com.ge.finance.spotlight.repositories.NotificationRepository;
import com.ge.finance.spotlight.repositories.ProcessRepository;
import com.ge.finance.spotlight.repositories.ProcessStepRepository;
import com.ge.finance.spotlight.repositories.ProcessUserRepository;
import com.ge.finance.spotlight.repositories.ScheduleDefinitionRepository;
import com.ge.finance.spotlight.repositories.ScheduleReportRepository;
import com.ge.finance.spotlight.repositories.ScheduledSubmissionRepository;
import com.ge.finance.spotlight.repositories.StatusRepository;
import com.ge.finance.spotlight.repositories.SubmissionRepository;
import com.ge.finance.spotlight.repositories.SubmissionRequestRepository;
import com.ge.finance.spotlight.repositories.SubmissionStepRepository;
import com.ge.finance.spotlight.requests.SubmissionStepRequest;
import com.ge.finance.spotlight.security.Constants;
import com.ge.finance.spotlight.services.SpotlightEmailService;
import com.ge.finance.spotlight.services.SubmissionStepService;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SubmissionCronJob {

    private static final long MINUTES_1 = 60_000;
    private static final long MINUTES_2 = 120_000;
    private static final long MINUTES_5 = 300_000;
    private static final long MINUTES_15 = 900_000;
    private static final long MINUTES_30 = 1_800_000;
    private static final long MINUTES_60 = 3_600_000;
    private static final long MINUTES_120 = 7_200_000;
    private static final long MINUTES_1_DAY = 86_400_000;

    private static final long FAILED_STATUS = Long.valueOf(4);

    private ScheduledSubmissionRepository scheduledSubmissionRepository;
    private NotificationRepository notificationRepository;
    private SpotlightEmailService spotlightEmailService;
    private SubmissionRepository submissionRepository;
    private ProcessRepository processRepository;
    private ProcessStepRepository processStepRepository;
    private AnalyticsReportRepository analyticsReportRepository;
    private ScheduleReportRepository scheduleReportRepository;
    private ScheduleDefinitionRepository scheduleDefinitionRepository;
    private ProcessUserRepository processUserRepository;
    private SubmissionStepRepository submissionStepRepository;
    private MessageGatewayRepository messageGatewayRepository;
    private NotificationLogRepository notificationLogRepository;
    private StatusRepository statusRepository;
    private SubmissionRequestRepository submissionRequestRepository;
    private SubmissionStepService submissionStepService;

    public SubmissionCronJob(ScheduledSubmissionRepository scheduledSubmissionRepository,
            NotificationRepository notificationRepository, SpotlightEmailService spotlightEmailService,
            SubmissionRepository submissionRepository, ProcessRepository processRepository,
            AnalyticsReportRepository analyticsReportRepository, ScheduleReportRepository scheduleReportRepository,
            ScheduleDefinitionRepository scheduleDefinitionRepository, ProcessUserRepository processUserRepository,
            SubmissionStepRepository submissionStepRepository, ProcessStepRepository processStepRepository,
            MessageGatewayRepository messageGatewayRepository, NotificationLogRepository notificationLogRepository,
            StatusRepository statusRepository, SubmissionRequestRepository submissionRequestRepository,
            SubmissionStepService submissionStepService) {
        this.scheduledSubmissionRepository = scheduledSubmissionRepository;
        this.notificationRepository = notificationRepository;
        this.spotlightEmailService = spotlightEmailService;
        this.submissionRepository = submissionRepository;
        this.processRepository = processRepository;
        this.analyticsReportRepository = analyticsReportRepository;
        this.scheduleReportRepository = scheduleReportRepository;
        this.scheduleDefinitionRepository = scheduleDefinitionRepository;
        this.processUserRepository = processUserRepository;
        this.submissionStepRepository = submissionStepRepository;
        this.processStepRepository = processStepRepository;
        this.messageGatewayRepository = messageGatewayRepository;
        this.notificationLogRepository = notificationLogRepository;
        this.statusRepository = statusRepository;
        this.submissionRequestRepository = submissionRequestRepository;
        this.submissionStepService = submissionStepService;
    }

    private Optional<Notification> getSMSNotificationForProcess(Long processId) {
        Optional<Notification> notifications = notificationRepository
                .findFirstByProcessIdAndSubmissionTypeAndEnableTextMessagingIsNotNull(processId,
                        Constants.DELAYED_SUBMISSION);
        if (notifications.isEmpty()) {
            return Optional.empty();
        } else {
            return notifications;
        }
    }

    private Optional<Notification> getSMSNotificationForProcessEscalation(Long processId, String escalationType) {
        Optional<Notification> notifications = notificationRepository
                .findByProcessIdAndEscalationTypeAndProcessStepIdIsNullAndStatusIdIsNullAndEnableTextMessagingIsNotNull(
                        processId, escalationType);
        if (notifications.isEmpty()) {
            return Optional.empty();
        } else {
            return notifications;
        }
    }

    /*
     * private Optional<SubmissionRequest> getSubmissionRequests(Date now, String
     * state) { Optional<SubmissionRequest> submissionRequests =
     * submissionRequestRepository
     * .findByStartTimeAndStateAndStartTimeGreaterThanOrderByStartTimeAsc(now,
     * Constants.QUEUED); if (submissionRequests.isEmpty()) { return
     * Optional.empty(); } else { return submissionRequests; } }
     */

    private void sendEmail(Notification notification, ScheduledSubmission scheduledSubmission) {
        String emailList = notification.getAdditionalEmails().trim() + " "
                + scheduledSubmission.getProcess().getSupportTeamEmail().trim();
        emailList = emailList.strip();
        if (!emailList.isEmpty()) {
            EmailModel emailModel = new EmailModel(notification.getNotificationTemplateID(), emailList, false);
            emailModel.schedSubmission = scheduledSubmission;
            spotlightEmailService.genericSend(emailModel);
        }
    }

    private void sendEmails(Notification notification, Submission submission, SubmissionStep submissionStep) {
        String emailList = notification.getAdditionalEmails().strip();
        Iterator<NotificationMobile> iNotificationMobile = notification.getUserMobiles().iterator();
        while (iNotificationMobile.hasNext()) {
            NotificationMobile tempNotificationMobile = iNotificationMobile.next();
            emailList += " " + tempNotificationMobile.getUser().getEmail();
        }
        emailList = emailList.strip();
        if (!emailList.isEmpty()) {
            EmailModel emailModel = new EmailModel(notification.getNotificationTemplateID(), emailList, false);
            emailModel.submission = submission;
            emailModel.submissionStep = submissionStep;
            emailModel.process = submission.getProcess();

            spotlightEmailService.genericSend(emailModel);

        }
    }

    private void sendSMS(Notification notification, ScheduledSubmission scheduledSubmission, Submission submission,
            SubmissionStep submissionStep) {

        if (!notification.getSubmissionType().isEmpty() && notification.getEscalationType().isEmpty()) {
            Iterator<NotificationMobile> iNotificationMobile = notification.getUserMobiles().iterator();
            while (iNotificationMobile.hasNext()) {
                NotificationMobile tempNotificationMobile = iNotificationMobile.next();
                User user = tempNotificationMobile.getUser();
                MessageGateway messageGateway = messageGatewayRepository.findById(user.getCarrier().getId())
                        .orElseThrow(NotFoundException::new);
                String smsGateway = user.getPhoneNumber() + messageGateway.getGateway();
                if (!smsGateway.isEmpty()) {
                    EmailModel emailModel = new EmailModel(notification.getNotificationTemplateID(), smsGateway, true);
                    emailModel.submission = submission;
                    emailModel.submissionStep = submissionStep;
                    emailModel.schedSubmission = scheduledSubmission;
                    spotlightEmailService.genericSend(emailModel);
                }
            }
        } else if (!notification.getEscalationType().isEmpty()) {
            Long templateId = (notification.getEscalationType().equalsIgnoreCase(Constants.DELAYED_ESCALATION)
                    ? NotificationTemplate.ESCALATION_DELAYED
                    : NotificationTemplate.ESCALATION_FAILED);
            Process process = (submission == null) ? scheduledSubmission.getProcess() : submission.getProcess();
            User appOwner = getAppOwner(process);
            String toEmails = (appOwner != null) ? (appOwner.getEmail()) : "";
            toEmails += " " + getManagerEmail(appOwner);
            EmailModel emailModel = new EmailModel(templateId, toEmails, false);
            emailModel.submission = submission;
            emailModel.schedSubmission = scheduledSubmission;

            Iterator<NotificationMobile> iNotificationMobile = notification.getUserMobiles().iterator();
            while (iNotificationMobile.hasNext()) {
                NotificationMobile tempNotificationMobile = iNotificationMobile.next();
                User user = tempNotificationMobile.getUser();
                MessageGateway messageGateway = messageGatewayRepository.findById(user.getCarrier().getId())
                        .orElseThrow(NotFoundException::new);
                String smsGateway = user.getPhoneNumber() + messageGateway.getGateway();

                if (!smsGateway.isEmpty()) {
                    emailModel.to = smsGateway;
                    emailModel.sms = true;
                    spotlightEmailService.genericSend(emailModel);
                }
            }

            String combinedToEmails = "";

            if (!notification.getAdditionalEmails().isEmpty()) {
                combinedToEmails = notification.getAdditionalEmails().trim();
            }

            if (templateId != NotificationTemplate.ESCALATION_DELAYED) {
                if (!toEmails.isEmpty()) {
                    combinedToEmails = combinedToEmails + " " + toEmails.trim();
                }
            }

            if (!combinedToEmails.isEmpty()) {
                emailModel.to = combinedToEmails.strip();
                emailModel.sms = false;
                spotlightEmailService.genericSend(emailModel);
            }
        }
    }

    @Scheduled(initialDelay = MINUTES_5, fixedRate = MINUTES_5) // 300_000 ms == 5 m
    public void compareSubmissions() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH) - 1); // starting yesterday
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        Date start = today.getTime();
        Date now = new Date(); // to right now
        System.out.println("Compare Submissions");
        List<ScheduledSubmission> scheduledSubmissions = scheduledSubmissionRepository
                .findByStartTimeIsBetweenAndSubmissionIdIsNullOrderByStartTimeAsc(start, now);

        for (ScheduledSubmission scheduledSubmission : scheduledSubmissions) {

            boolean optNotification = notificationLogRepository
                    .existsByScheduledSubmissionIdAndNotificationTemplateAndProcessIdIsNotNull(
                            scheduledSubmission.getId(), NotificationTemplate.DELAYED_SUBMISSION);
            long startDiff = now.getTime() - scheduledSubmission.getStartTime().getTime();
            User appOwner = getAppOwner(scheduledSubmission.getProcess());
            boolean skipEmail = false;
            String toEmails = (appOwner != null) ? (appOwner.getEmail()) : "";
            if (startDiff > MINUTES_15) {
                System.out.println(String.format("WARNING: Scheduled submission %d has not yet started",
                        scheduledSubmission.getId()));
                if (!optNotification) {
                    notificationRepository
                            .findFirstByProcessIdAndSubmissionType(scheduledSubmission.getProcess().getId(),
                                    Constants.DELAYED_ESCALATION)
                            .ifPresent(notification -> {
                                sendEmail(notification, scheduledSubmission);
                            });
                    ;
                    getSMSNotificationForProcess(scheduledSubmission.getProcess().getId()).ifPresent(notifications -> {
                        sendSMS(notifications, scheduledSubmission, null, null);
                    });
                }
                if (scheduledSubmission.getAckDelayedEmailStatus() == 'A'
                        || scheduledSubmission.getAckDelayedEmailStatus() == 'B') {
                    skipEmail = true;
                } else {
                    scheduledSubmission.setAckDelayedEmailStatus('A');
                }
            } else {
                continue;
            }
            if (startDiff > MINUTES_30 && scheduledSubmission.getAckDelayedEmailStatus() != 'B' && appOwner != null) {
                System.out.println("Delayed > 30 minutes");
                toEmails += " " + getManagerEmail(appOwner);
                skipEmail = false;
                scheduledSubmission.setAckDelayedEmailStatus('B');
            }

            if (scheduledSubmission.getProcess().getSubmissionDelayedEscalationAlrt()
                    && !scheduledSubmission.getAcknowledgementFlag() && !skipEmail && appOwner != null) {
                System.out.println("Delayed submission for " + toEmails);                
                EmailModel emailModel = new EmailModel(NotificationTemplate.ESCALATION_DELAYED, toEmails, false);
                emailModel.schedSubmission = scheduledSubmission;
                spotlightEmailService.genericSend(emailModel);
                getSMSNotificationForProcessEscalation(scheduledSubmission.getProcess().getId(),
                        Constants.DELAYED_ESCALATION).ifPresent(notifications -> {
                            this.sendSMS(notifications, scheduledSubmission, null, null);
                        });
                scheduledSubmissionRepository.save(scheduledSubmission);
            }
        }
    }

    @Scheduled(initialDelay = MINUTES_1_DAY, fixedRate = MINUTES_1_DAY) // 86_400_000 ms == 1 Day
    public void scheduleProcessSummaryReport() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        Date start = today.getTime();
        Date now = today.getTime(); // to right now

        List<AnalyticsReport> scheduleReportsList = analyticsReportRepository
                .findByStartTimeIsBetweenOrderByStartTimeAsc(start, now);

        System.out.println("Schedule Process Summary Report");
        for (AnalyticsReport analyticsReport : scheduleReportsList) {
            if (analyticsReport != null) {

                ScheduleReport scheduledReport = scheduleReportRepository
                        .findById(analyticsReport.getScheduleReportId()).orElseThrow(NotFoundException::new);
                Process process = processRepository.findById(scheduledReport.getProcessId()).get();
                String emailList = scheduledReport.getAdditionalEmails() + " " + process.getSupportTeamEmail();
                spotlightEmailService.sendProcessSummaryReport(NotificationTemplate.PROCESS_SUMMARY, analyticsReport,
                        emailList);
            }
        }
    }

    @Scheduled(initialDelay = MINUTES_2, fixedRate = MINUTES_15)
    public void failedSubmissions() {
        Calendar yesterday = Calendar.getInstance();
        yesterday.set(Calendar.DAY_OF_MONTH, yesterday.get(Calendar.DAY_OF_MONTH) - 1); // starting yesterday
        yesterday.set(Calendar.HOUR_OF_DAY, 0);
        yesterday.set(Calendar.MINUTE, 0);
        yesterday.set(Calendar.SECOND, 0);
        yesterday.set(Calendar.MILLISECOND, 0);
        List<Submission> failedSubmissions = submissionRepository
                .findByProcessSubmissionEscalationAlrtAndStatusIdAndAcknowledgementFlagAndAckFailedEmailStatusIsNot('Y', FAILED_STATUS, false, 'B', yesterday.getTime());
        System.out.println("Failed Submission found " + failedSubmissions.size());
        Date now = new Date(); // to right now
        for (Submission failedSubmission : failedSubmissions) {        

            System.out.println("Submission id: " + failedSubmission.getId());
            User appOwner = getAppOwner(failedSubmission.getProcess());

            // No AppOwner assign? Next Submission
            if (appOwner == null) {
                System.out.println("No App Owner");
                continue;
            }

            // Look for failed step end date
            Date failedStepEndDate = new Date();
            Iterator<SubmissionStep> steps = failedSubmission.getSteps().iterator();
            while (steps.hasNext()) {
                SubmissionStep currentStep = steps.next();
                if (currentStep.getStatus().getId().equals(FAILED_STATUS)) {
                    failedStepEndDate = currentStep.getEndTime();
                    break;
                }
            }

            long stepDiff = now.getTime() - failedStepEndDate.getTime();
            System.out.println("Step Diff: " + stepDiff);
            String toEmails = appOwner.getEmail();
            if (stepDiff > MINUTES_30) {
                System.out.println("> 30 minutes");
                // Add App Owner Manager mail
                toEmails += " " + getManagerEmail(appOwner);
                failedSubmission.setAckfailedEmailStatus('B');
                submissionRepository.save(failedSubmission);
            } else if (stepDiff > MINUTES_15) {
                System.out.println("> 15 minutes");
                failedSubmission.setAckfailedEmailStatus('A');
                submissionRepository.save(failedSubmission);
            } else {
                continue;
            }

            System.out.println(toEmails);            
            EmailModel emailModel = new EmailModel(NotificationTemplate.ESCALATION_FAILED, toEmails, false);
            emailModel.submission = failedSubmission;
            spotlightEmailService.genericSend(emailModel);
            getSMSNotificationForProcessEscalation(failedSubmission.getProcess().getId(), Constants.FAILED_ESCALATION)
                    .ifPresent(notifications -> {
                        sendSMS(notifications, null, failedSubmission, null);
                    });
        }
    }

    private User getAppOwner(Process process) {
        User appOwner = null;
        if (process.getAppOwner() != null) {
            appOwner = process.getAppOwner();
        } else if (process.getSender() != null && process.getSender().getAppOwner() != null) {
            appOwner = process.getSender().getAppOwner();
        }
        return appOwner;
    }

    private String getManagerEmail(User appOwner) {
        try {
            String managerSSO = GEOneHRConnection.getManagerSSOFromOneHR(appOwner.getSso(), "");
            return managerSSO + "@ge.com";
        } catch (Exception e) {
            return "";
        }
    }

    @Scheduled(initialDelay = MINUTES_15, fixedRate = MINUTES_15) // 15 m
    public void failedAcknowledgedSubmissions() {
        List<ProcessDTO> processes = processRepository.findBySubmissionEscalationAlrt('Y');
        Date now = new Date();
        for (ProcessDTO process : processes) {

            Submission failedAcknowledgedSubmission = submissionRepository
                    .findFirstByProcessIdAndStatusIdAndAckFailedEmailStatusIsNotAndAcknowledgementFlagTrueOrderByIdDesc(
                            process.getId(), FAILED_STATUS, 'D');
            if (failedAcknowledgedSubmission == null) {
                continue;
            }
            System.out.println("Failed Acknowledged Submission found " + failedAcknowledgedSubmission.getId()
                    + " For process: " + process.getName());

            User appOwner = getAppOwner(failedAcknowledgedSubmission.getProcess());

            // No AppOwner assign? Next Submission
            if (appOwner == null) {
                System.out.println("No App Owner");
                continue;
            }

            Submission newSubmission = submissionRepository.findFirstByProcessIdAndStatusIdIsNotAndIdGreaterThan(
                    failedAcknowledgedSubmission.getProcess().getId(), FAILED_STATUS,
                    failedAcknowledgedSubmission.getId());
            if (newSubmission == null) {
                System.out.println("No New submission");
                if (failedAcknowledgedSubmission.getAcknowledgementDate() == null)
                    continue;
                String toEmails = appOwner.getEmail();
                long diff = now.getTime() - failedAcknowledgedSubmission.getAcknowledgementDate().getTime();
                if (diff > MINUTES_120) {
                    // Send mail to App Owner Manager
                    System.out.println("> 120 minutes");
                    toEmails += " " + getManagerEmail(appOwner);
                    failedAcknowledgedSubmission.setAckfailedEmailStatus('D');
                    submissionRepository.save(failedAcknowledgedSubmission);
                } else if (diff > MINUTES_60 && failedAcknowledgedSubmission.getAckfailedEmailStatus() != 'C') {
                    System.out.println("> 60 minutes");
                    failedAcknowledgedSubmission.setAckfailedEmailStatus('C');
                    submissionRepository.save(failedAcknowledgedSubmission);
                } else {
                    continue;
                }
                System.out.println(toEmails);                
                EmailModel emailModel = new EmailModel(NotificationTemplate.ESCALATION_FAILED_ACK, toEmails, false);
                emailModel.submission = failedAcknowledgedSubmission;
                spotlightEmailService.genericSend(emailModel);
            } else {
                System.out.println("New non-failed submissions submitted");
            }

        }
    }

    @Scheduled(initialDelay = MINUTES_1_DAY, fixedRate = MINUTES_1_DAY)
    public void missingSchedulesNotifications() {
        List<ProcessUser> processUser = null;
        Calendar scheduleEndDate = Calendar.getInstance();
        Calendar scheduleEndPlusTenDate = Calendar.getInstance();
        Date now = new Date();
        String toEmails = null;
        List<Process> processes = processRepository.findAll();
        for (Process process : processes) {
            List<ScheduleDefinition> scheduleList = scheduleDefinitionRepository
                    .findByProcessIdOrderByScheduleEndDateDesc(process.getId());
            if (scheduleList.size() > 0) {
                scheduleEndDate.setTime(scheduleList.get(0).getScheduleEndDate());
                scheduleEndPlusTenDate.setTime(scheduleList.get(0).getScheduleEndDate());
                scheduleEndPlusTenDate.add(Calendar.DATE, 10);
                if (scheduleList.get(0).getRecurrencePattern() == 'C') { // Missing Notification For Custom Recurrance
                                                                         // Pattern
                    if (scheduleEndDate.equals(now)) { // Notification for last schdeule submission
                        processUser = processUserRepository.findByProcessId(scheduleList.get(0).getProcess().getId());
                        if (!processUser.isEmpty()) {
                            for (ProcessUser proUserList : processUser) {
                                if (proUserList != null) {
                                    toEmails = (proUserList.getUser() != null) ? (proUserList.getUser().getEmail())
                                            : "";
                                    EmailModel emailModel = new EmailModel(NotificationTemplate.MISSING_NOTIFICATION,
                                            toEmails, false);
                                    emailModel.process = process;
                                    spotlightEmailService.genericSend(emailModel);
                                } else {
                                    continue;
                                }
                            }
                        } else {
                            continue;
                        }
                    } else if (scheduleEndPlusTenDate.equals(now)) { // Notification for last 10 days missing schedules
                        User appOwner = getAppOwner(process);
                        toEmails = (appOwner != null) ? (appOwner.getEmail()) : "";
                        EmailModel emailModel = new EmailModel(NotificationTemplate.MISSING_NOTIFICATION, toEmails,
                                false);
                        emailModel.process = process;
                        spotlightEmailService.genericSend(emailModel);
                    } else {
                        continue;
                    }
                } else { // Missing Notification For Monthly,Weekly Recurrance Pattern
                    Calendar scheduleEndPlusThirtyDate = Calendar.getInstance();
                    scheduleEndPlusThirtyDate.setTime(scheduleEndDate.getTime());
                    scheduleEndPlusThirtyDate.add(Calendar.DATE, -30);

                    Calendar scheduleEndPlusThreeDate = Calendar.getInstance();
                    scheduleEndPlusThreeDate.setTime(scheduleEndDate.getTime());
                    scheduleEndPlusThreeDate.add(Calendar.DATE, -3);
                    if (scheduleEndPlusThirtyDate.equals(now)) { // if last occurance is within next 30 days
                        processUser = processUserRepository.findByProcessId(scheduleList.get(0).getProcess().getId());
                        if (!processUser.isEmpty()) {
                            for (ProcessUser proUserList : processUser) {
                                toEmails = (proUserList.getUser() != null) ? (proUserList.getUser().getEmail()) : "";
                                EmailModel emailModel = new EmailModel(NotificationTemplate.MISSING_NOTIFICATION,
                                        toEmails, false);
                                emailModel.process = process;
                                spotlightEmailService.genericSend(emailModel);
                            }
                        } else {
                            continue;
                        }
                    } else if (scheduleEndPlusThreeDate.equals(now)) { // if last occurance is within next 3 days
                        User appOwner = getAppOwner(process);
                        toEmails = (appOwner != null) ? (appOwner.getEmail()) : "";
                        EmailModel emailModel = new EmailModel(NotificationTemplate.MISSING_NOTIFICATION, toEmails,
                                false);
                        emailModel.process = process;
                        spotlightEmailService.genericSend(emailModel);
                    } else {
                        continue;
                    }
                }
            }

        }
    }

    @Scheduled(initialDelay = MINUTES_1, fixedRate = MINUTES_1)
    public void longRunSubmissions() {
        Date now = new Date();
        Date oneMonth = new Date();

        Calendar c = new GregorianCalendar();
        c.add(Calendar.DATE, -2);
        oneMonth = c.getTime();

        StringBuilder submissionList = new StringBuilder();
        List<Submission> runningSubmissions = submissionRepository
                .findByEndTimeIsNullAndStartTimeGreaterThanOrderByStartTimeDesc(oneMonth);
        for (Submission submission : runningSubmissions) {
            submissionList.append(submission.getId().toString() + ", ");
            boolean optNotification = notificationLogRepository
                    .existsBySubmissionIdAndProcessStepIdIsNull(submission.getId());
            Process process = submission.getProcess();

            if (!optNotification && process.getLongRunningSubAlrt()) {
                Optional<ScheduledSubmission> optScheduledSubmission = scheduledSubmissionRepository
                        .findBySubmissionId(submission.getId());

                if (optScheduledSubmission.isPresent()) {
                    ScheduledSubmission scheduledSubmission = optScheduledSubmission.get();

                    long scheduledDuration = (scheduledSubmission.getEndTime().getTime()
                            - scheduledSubmission.getStartTime().getTime())
                            + (scheduledSubmission.getTolerance() * MINUTES_1);
                    long actualDuration = now.getTime() - submission.getStartTime().getTime();

                    if (scheduledDuration > 0 && actualDuration > scheduledDuration) {

                        String toEmails = this.getToEmails(submission.getProcess());

                        EmailModel emailModel = new EmailModel(NotificationTemplate.LONG_RUN_SUBMISSION, toEmails,
                                false);
                        emailModel.submission = submission;

                        spotlightEmailService.genericSend(emailModel);

                        getToEmailsSubmissionTypeAlerts(submission.getProcess().getId(),
                                Constants.LONG_RUNNING_SUBMISSION).ifPresent(notifications -> {
                                    sendEmails(notifications, submission, null);
                                    sendSMS(notifications, null, submission, null);
                                });
                    }
                } else {
                    long actualDuration = now.getTime() - submission.getStartTime().getTime();

                    if (actualDuration > MINUTES_1_DAY) { // Submission running more than one entire day
                        String toEmails = this.getToEmails(submission.getProcess());

                        EmailModel emailModel = new EmailModel(NotificationTemplate.LONG_RUN_SUBMISSION, toEmails,
                                false);
                        emailModel.submission = submission;
                        emailModel.process = submission.getProcess();
                        spotlightEmailService.genericSend(emailModel);

                        getToEmailsSubmissionTypeAlerts(submission.getProcess().getId(),
                                Constants.LONG_RUNNING_SUBMISSION).ifPresent(notifications -> {
                                    sendEmails(notifications, submission, null);
                                    sendSMS(notifications, null, submission, null);
                                });
                    }
                }
            }

            if (submission.getAdHoc() && (process.getMaxRunTimeHours() > 0 || process.getMaxRunTimeMinutes() > 0)) {
                long totalMaxDuration = (process.getMaxRunTimeHours() * 3600000)
                        + (process.getMaxRunTimeMinutes() * 60000);
                long submissionStartTime = submission.getStartTime().getTime();

                if (now.getTime() - submissionStartTime > totalMaxDuration) {
                    System.out.println("==LONG RUN ADHOC== for submission: " + submission.getId());
                    // Check if notification was already send
                    boolean wasNotificationSend = notificationLogRepository.existsBySubmissionIdAndNotificationTemplate(
                            submission.getId(), NotificationTemplate.LONG_RUN_ADHOC_SUBMISSION);
                    if (!wasNotificationSend) {
                        String toEmails = this.getToEmails(submission.getProcess());
                        EmailModel emailModel = new EmailModel(NotificationTemplate.LONG_RUN_ADHOC_SUBMISSION, toEmails,
                                false);
                        emailModel.submission = submission;
                        emailModel.process = submission.getProcess();
                        spotlightEmailService.genericSend(emailModel);
                    }
                }
            }

            if (process.getLongRunningStepAlrt()) {
                List<SubmissionStep> submissionSteps = submissionStepRepository
                        .findBySubmissionIdOrderByIdAsc(submission.getId());

                // Review Submission steps per Submission
                for (SubmissionStep submissionStep : submissionSteps) {

                    boolean optNotificationStep = notificationLogRepository.existsBySubmissionIdAndProcessStepId(
                            submission.getId(), submissionStep.getProcessStep().getId());

                    if (!optNotificationStep) {
                        if (submissionStep.getStatus().getName().equalsIgnoreCase("in progress")) {

                            long actualDuration = now.getTime() - submissionStep.getStartTime().getTime();

                            Long duration = (submissionStep.getProcessStep().getManualDuration() == null)
                                    ? submissionStep.getProcessStep().getDuration()
                                    : submissionStep.getProcessStep().getManualDuration();

                            if (duration > 0 && actualDuration > (duration * MINUTES_1)) { // Step
                                                                                           // running
                                // more than one entire day
                                String toEmails = this.getToEmails(submission.getProcess());

                                EmailModel emailModel = new EmailModel(
                                        NotificationTemplate.LONG_RUN_SUBMISSION_STEP_DELAYED, toEmails, false);
                                emailModel.submission = submission;
                                emailModel.process = submission.getProcess();
                                emailModel.submissionStep = submissionStep;
                                spotlightEmailService.genericSend(emailModel);

                                getToEmailsSubmissionTypeAlerts(submission.getProcess().getId(),
                                        Constants.LONG_RUNNING_STEPS).ifPresent(notifications -> {
                                            sendEmails(notifications, submission, submissionStep);
                                            sendSMS(notifications, null, submission, submissionStep);
                                        });
                            }
                        }

                    }
                }

            }
        }
        System.out.println("Long Running Submission - Submissions #" + submissionList);
    }

    @Scheduled(initialDelay = MINUTES_1, fixedRate = MINUTES_1)
    public void maxRunlongRunningSubmission() {

        Date now = new Date();
        List<Process> processList = processRepository.findAll();
        for (Process process : processList) {
            if ((process.getMaxRunTimeHours() != null && process.getMaxRunTimeHours() > 0)
                    || (process.getMaxRunTimeMinutes() != null && process.getMaxRunTimeMinutes() > 0)) {
                List<Submission> runningSubmissions = submissionRepository.findByProcessIdAndStatusId(process.getId(),
                        Status.IN_PROGRESS);
                if (!runningSubmissions.isEmpty()) {
                    for (Submission submission : runningSubmissions) {
                        long totalMaxDuration = (process.getMaxRunTimeHours() * 3600000)
                                + (process.getMaxRunTimeMinutes() * 60000);
                        long submissionStartTime = submission.getStartTime().getTime();
                        if (now.getTime() - submissionStartTime > totalMaxDuration) {
                            System.out.println("==LONG RUN == for submission: " + submission.getId());
                            submission.setStatus(
                                    statusRepository.findById(Status.LONG_RUNNING).orElseThrow(NotFoundException::new));
                            submissionRepository.save(submission);
                            // Check if notification was already send
                            boolean wasNotificationSend = notificationLogRepository
                                    .existsBySubmissionIdAndNotificationTemplate(submission.getId(),
                                            NotificationTemplate.LONG_RUN_SUBMISSION);
                            if (!wasNotificationSend) {
                                String toEmails = this.getToEmails(submission.getProcess());
                                EmailModel emailModel = new EmailModel(NotificationTemplate.LONG_RUN_SUBMISSION,
                                        toEmails, false);
                                emailModel.submission = submission;
                                emailModel.process = submission.getProcess();
                                spotlightEmailService.genericSend(emailModel);
                            }
                        }
                    }
                }
            }
        }
    }

    private String getToEmails(Process process) {
        User appOwner = this.getAppOwner(process);
        String toEmails = (appOwner != null) ? appOwner.getEmail() : "";
        toEmails = toEmails + " " + process.getSupportTeamEmail() + " " + "jamie.myers@ge.com";
        return toEmails;
    }

    @Scheduled(initialDelay = MINUTES_1_DAY, fixedRate = MINUTES_1_DAY) // 1 day
    public void calculateStepDuration() {

        Iterable<ProcessStep> processSteps = processStepRepository.findAll();
        for (ProcessStep processStep : processSteps) {
            long count = 0;
            long totalTime = 0;
            long avgduration = 0;
            long diff = 0;
            List<SubmissionStep> submissionSteps = submissionStepRepository
                    .findByProcessStepIdAndEndTimeIsNotNull(processStep.getId());

            for (SubmissionStep submissionStep : submissionSteps) {

                diff = submissionStep.getEndTime().getTime() - submissionStep.getStartTime().getTime();

                totalTime = totalTime + diff;
                count++;
            }

            if (count > 0) {
                avgduration = (totalTime / count) / MINUTES_1; // Calculates it in minutes

                processStep.setDuration(avgduration);

                processStepRepository.save(processStep);
            }
        }
    }

    private Optional<Notification> getToEmailsSubmissionTypeAlerts(Long processId, String submissionType) {
        Optional<Notification> notification = notificationRepository
                .findFirstByProcessIdAndSubmissionTypeAndProcessStepIdIsNullAndStatusIdIsNull(processId,
                        submissionType);
        if (notification.isEmpty()) {
            return Optional.empty();
        } else {
            return notification;
        }
    }
}
