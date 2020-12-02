package com.ge.finance.spotlight.cronjobs;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.ge.finance.spotlight.exceptions.NotFoundException;
import com.ge.finance.spotlight.libs.GEOneHRConnection;
import com.ge.finance.spotlight.models.AnalyticsReport;
import com.ge.finance.spotlight.models.MessageGateway;
import com.ge.finance.spotlight.models.Notification;
import com.ge.finance.spotlight.models.NotificationTemplate;
import com.ge.finance.spotlight.models.Process;
import com.ge.finance.spotlight.models.ProcessStep;
import com.ge.finance.spotlight.models.ProcessUser;
import com.ge.finance.spotlight.models.ScheduleDefinition;
import com.ge.finance.spotlight.models.ScheduleReport;
import com.ge.finance.spotlight.models.ScheduledSubmission;
import com.ge.finance.spotlight.models.Submission;
import com.ge.finance.spotlight.models.SubmissionStep;
import com.ge.finance.spotlight.models.User;
import com.ge.finance.spotlight.models.NotificationLog;
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
import com.ge.finance.spotlight.repositories.SubmissionRepository;
import com.ge.finance.spotlight.repositories.SubmissionStepRepository;

import com.ge.finance.spotlight.repositories.UserRepository;
import com.ge.finance.spotlight.services.SpotlightEmailService;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SubmissionCronJob {

    private static final long MINUTES_1 = 60_000;
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
    private UserRepository userRepository;
    private NotificationLogRepository notificationLogRepository;

    private ScheduledSubmission scheduledSubmission;

    public SubmissionCronJob(ScheduledSubmissionRepository scheduledSubmissionRepository,
            NotificationRepository notificationRepository, SpotlightEmailService spotlightEmailService,
            SubmissionRepository submissionRepository, ProcessRepository processRepository,
            AnalyticsReportRepository analyticsReportRepository, ScheduleReportRepository scheduleReportRepository,
            ScheduleDefinitionRepository scheduleDefinitionRepository, ProcessUserRepository processUserRepository,
            SubmissionStepRepository submissionStepRepository, ProcessStepRepository processStepRepository,
            MessageGatewayRepository messageGatewayRepository, UserRepository userRepository,
            NotificationLogRepository notificationLogRepository) {
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
        this.userRepository = userRepository;
        this.notificationLogRepository = notificationLogRepository;
    }

    private Optional<Notification> getNotificationForProcess(Long processId) {
        List<Notification> notifications = notificationRepository
                .findByProcessIdAndProcessStepIdIsNullAndStatusIdIsNull(processId);
        if (notifications.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(notifications.get(0));
        }
    }

    private Optional<List<Notification>> getSMSNotificationForProcess(Long processId) {
        List<Notification> notifications = notificationRepository
                .findByProcessIdAndProcessStepIdIsNullAndStatusIdIsNullAndEnableTextMessagingIsNotNull(processId);
        if (notifications.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(notifications);
        }
    }

    private void sendEmail(Notification notification) {
        String emailList = notification.getAdditionalEmails().trim() + " "
                + this.scheduledSubmission.getProcess().getSupportTeamEmail().trim();
        emailList = emailList.trim();
        if (!emailList.isEmpty()) {
            spotlightEmailService.genericSend(notification.getNotificationTemplateID(), emailList,
                    this.scheduledSubmission, null, null, null, null, null, null);
        }
    }

    private void sendSMS(List<Notification> notificationList) {
        for (Notification notification : notificationList) {
            if (notification.getCreatedFor() != null) {
                User user = userRepository.findFirstBySso(notification.getCreatedFor().getSso());
                MessageGateway messageGateway = messageGatewayRepository.findById(user.getCarrier().getId())
                        .orElseThrow(NotFoundException::new);
                String smsGateway = notification.getCreatedFor().getPhoneNumber() + messageGateway.getGateway();
                if (!smsGateway.isEmpty()) {
                    spotlightEmailService.genericSMSSend(notification.getNotificationTemplateID(), smsGateway,
                            this.scheduledSubmission, null, null, null, null, null);
                }
                if (!notification.getAdditionalEmails().isEmpty()) {
                    spotlightEmailService.genericSMSSend(notification.getNotificationTemplateID(),
                            notification.getAdditionalEmails().trim(), this.scheduledSubmission, null, null, null, null,
                            null);
                }
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
                .findByStartTimeIsBetweenOrderByStartTimeAsc(start, now);

        for (ScheduledSubmission scheduledSubmission : scheduledSubmissions) {
            if (scheduledSubmission.getSubmission() == null) { // submission has not yet started
                List<NotificationLog> optNotification = notificationLogRepository
                        .findByScheduledSubmissionIdAndNotificationTemplateAndProcessIdIsNotNull(
                                scheduledSubmission.getId(), NotificationTemplate.DELAYED_SUBMISSION);
                long startDiff = now.getTime() - scheduledSubmission.getStartTime().getTime();
                User appOwner = getAppOwner(scheduledSubmission.getProcess());
                boolean skipEmail = false;
                String toEmails = (appOwner != null) ? (appOwner.getEmail()) : "";
                if (startDiff > MINUTES_15) {
                    System.out.println(String.format("WARNING: Scheduled submission %d has not yet started",
                            scheduledSubmission.getId()));
                    this.scheduledSubmission = scheduledSubmission;
                    if (optNotification.size() <= 0) {
                        getNotificationForProcess(scheduledSubmission.getProcess().getId()).ifPresent(this::sendEmail);
                        getSMSNotificationForProcess(scheduledSubmission.getProcess().getId()).ifPresent(this::sendSMS);
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
                if (startDiff > MINUTES_30 && scheduledSubmission.getAckDelayedEmailStatus() != 'B'
                        && appOwner != null) {
                    System.out.println("Delayed > 30 minutes");
                    toEmails += " " + getManagerEmail(appOwner);
                    skipEmail = false;
                    scheduledSubmission.setAckDelayedEmailStatus('B');
                }

                if (!scheduledSubmission.getAcknowledgementFlag() && !skipEmail && appOwner != null) {
                    System.out.println("Delayed submission for " + toEmails);
                    spotlightEmailService.genericSend(NotificationTemplate.ESCALATION_DELAYED, toEmails,
                            scheduledSubmission, null, null, null, null, null, null);
                    scheduledSubmissionRepository.save(scheduledSubmission);
                }
            } /* */
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

    @Scheduled(initialDelay = MINUTES_15, fixedRate = MINUTES_15) // 15 m
    public void failedSubmissions() {
        List<Submission> failedSubmissions = submissionRepository
                .findByStatusIdAndAcknowledgementFlagAndAckFailedEmailStatusIsNot(FAILED_STATUS, false, 'B');
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
            spotlightEmailService.genericSend(NotificationTemplate.ESCALATION_FAILED, toEmails, null, failedSubmission,
                    null, null, null, null, null);
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
        List<Process> processes = processRepository.findAll();
        Date now = new Date();
        for (Process process : processes) {
            if (!process.getSubmissionEscalationAlrt())
                continue;

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
                } else if (diff > MINUTES_60) {
                    System.out.println("> 60 minutes");
                    failedAcknowledgedSubmission.setAckfailedEmailStatus('C');
                    submissionRepository.save(failedAcknowledgedSubmission);
                } else {
                    continue;
                }
                System.out.println(toEmails);
                spotlightEmailService.genericSend(NotificationTemplate.ESCALATION_FAILED, toEmails, null,
                        failedAcknowledgedSubmission, null, null, null, null, null);
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
                                    spotlightEmailService.genericSend(NotificationTemplate.MISSING_NOTIFICATION,
                                            toEmails, null, null, process, null, null, null, null);
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
                        spotlightEmailService.genericSend(NotificationTemplate.MISSING_NOTIFICATION, toEmails, null,
                                null, process, null, null, null, null);
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
                                spotlightEmailService.genericSend(NotificationTemplate.MISSING_NOTIFICATION, toEmails,
                                        null, null, process, null, null, null, null);
                            }
                        } else {
                            continue;
                        }
                    } else if (scheduleEndPlusThreeDate.equals(now)) { // if last occurance is within next 3 days
                        User appOwner = getAppOwner(process);
                        toEmails = (appOwner != null) ? (appOwner.getEmail()) : "";
                        spotlightEmailService.genericSend(NotificationTemplate.MISSING_NOTIFICATION, toEmails, null,
                                null, process, null, null, null, null);
                    } else {
                        continue;
                    }
                }
            }

        }
    }

    @Scheduled(initialDelay = MINUTES_1, fixedRate = MINUTES_1) // 5 minutes
    public void longRunSubmissions() {
        Date now = new Date();
        Date oneMonth = new Date();

        Calendar c = new GregorianCalendar();
        c.add(Calendar.DATE, -30);
        oneMonth = c.getTime();

        List<Submission> runningSubmissions = submissionRepository
                .findByEndTimeIsNullAndStartTimeGreaterThanOrderByStartTimeDesc(oneMonth);
        for (Submission submission : runningSubmissions) {
            System.out.println("Long Running Submission - Submission #" + submission.getId());
            List<NotificationLog> optNotification = notificationLogRepository
                    .findBySubmissionIdAndProcessStepIdIsNull(submission.getId());
            Process process = submission.getProcess();

            if ((optNotification.size() == 0) && process.getLongRunningSubAlrt()) {
                Optional<ScheduledSubmission> optScheduledSubmission = scheduledSubmissionRepository
                        .findBySubmissionId(submission.getId());

                if (optScheduledSubmission.isPresent()) {
                    ScheduledSubmission scheduledSubmission = optScheduledSubmission.get();

                    long scheduledDuration = (scheduledSubmission.getEndTime().getTime()
                            - scheduledSubmission.getStartTime().getTime()) + scheduledSubmission.getTolerance();
                    long actualDuration = now.getTime() - submission.getStartTime().getTime();

                    if (scheduledDuration > 0 && actualDuration > scheduledDuration) {
                        User appOwner = this.getAppOwner(submission.getProcess());
                        String toEmails = (appOwner != null) ? (appOwner.getEmail()) : "";

                        toEmails = toEmails + " " + submission.getProcess().getSupportTeamEmail();

                        spotlightEmailService.genericSend(NotificationTemplate.LONG_RUN_SUBMISSION, toEmails, null,
                                submission, null, null, null, null, null);
                    }
                } else {
                    long actualDuration = now.getTime() - submission.getStartTime().getTime();

                    if (actualDuration > MINUTES_1_DAY) { // Submission running more than one entire day
                        User appOwner = this.getAppOwner(submission.getProcess());
                        String toEmails = (appOwner != null) ? appOwner.getEmail() : "";

                        toEmails = toEmails + " " + submission.getProcess().getSupportTeamEmail();

                        spotlightEmailService.genericSend(NotificationTemplate.LONG_RUN_SUBMISSION, toEmails, null,
                                submission, submission.getProcess(), appOwner, null, null, null);                                           
                    }
                }
            }
            
            if (submission.getAdHoc() && (process.getMaxRunTimeHours() > 0 || process.getMaxRunTimeMinutes() > 0)) {                
                long totalMaxDuration = (process.getMaxRunTimeHours() * 3600000) + (process.getMaxRunTimeMinutes() * 60000);
                long submissionStartTime = submission.getStartTime().getTime();

                if (now.getTime() - submissionStartTime > totalMaxDuration) {
                    System.out.println("==LONG RUN ADHOC== for submission: " + submission.getId());
                    // Check if notification was already send
                    boolean wasNotificationSend = notificationLogRepository.existsBySubmissionIdAndNotificationTemplate(submission.getId(), NotificationTemplate.LONG_RUN_ADHOC_SUBMISSION);
                    if (!wasNotificationSend) {
                        User appOwner = this.getAppOwner(submission.getProcess());
                        String toEmails = (appOwner != null) ? (appOwner.getEmail()) : "";

                        toEmails = toEmails + " " + submission.getProcess().getSupportTeamEmail();

                        spotlightEmailService.genericSend(NotificationTemplate.LONG_RUN_ADHOC_SUBMISSION, toEmails, null,
                                submission, submission.getProcess(), appOwner, null, null, null);
                    }
                }
            }

            if (process.getLongRunningStepAlrt()) {
                List<SubmissionStep> submissionSteps = submissionStepRepository
                        .findBySubmissionIdOrderByIdAsc(submission.getId());

                // Review Submission steps per Submission
                for (SubmissionStep submissionStep : submissionSteps) {

                    List<NotificationLog> optNotificationStep = notificationLogRepository
                            .findBySubmissionIdAndProcessStepId(submission.getId(),
                                    submissionStep.getProcessStep().getId());

                    if (optNotificationStep.size() == 0) {
                        if (submissionStep.getStatus().getName().equalsIgnoreCase("in progress")) {

                            long actualDuration = now.getTime() - submissionStep.getStartTime().getTime();

                            if (submissionStep.getProcessStep().getDuration() > 0
                                    && actualDuration > (submissionStep.getProcessStep().getDuration() * MINUTES_1)) { // Step
                                                                                                                       // running
                                // more than
                                // one
                                // entire day
                                User appOwner = this.getAppOwner(submission.getProcess());
                                String toEmails = (appOwner != null) ? (appOwner.getEmail()) : "";

                                toEmails = toEmails + " " + submission.getProcess().getSupportTeamEmail();

                                spotlightEmailService.genericSend(NotificationTemplate.SUBMISSION_STEP_DELAYED,
                                        toEmails, null, submission, submission.getProcess(), appOwner, submissionStep,
                                        null, null);
                            }
                        }

                    }
                }

            }
        }
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

}
