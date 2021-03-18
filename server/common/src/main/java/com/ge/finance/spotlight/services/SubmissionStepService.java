package com.ge.finance.spotlight.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ge.finance.spotlight.dto.EmailModel;
import com.ge.finance.spotlight.exceptions.ConflictException;
import com.ge.finance.spotlight.exceptions.NotFoundException;
import com.ge.finance.spotlight.models.*;
import com.ge.finance.spotlight.models.Process;
import com.ge.finance.spotlight.repositories.*;
import com.ge.finance.spotlight.requests.SubmissionStepRequest;
import com.ge.finance.spotlight.security.Constants;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.System;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SubmissionStepService {

    private static final long MINUTES_1 = 60_000;
    private static Map<String, Integer> MONTHS = new HashMap<>();
    static {
        MONTHS.put("jan", 1);
        MONTHS.put("feb", 2);
        MONTHS.put("mar", 3);
        MONTHS.put("apr", 4);
        MONTHS.put("may", 5);
        MONTHS.put("jun", 6);
        MONTHS.put("jul", 7);
        MONTHS.put("aug", 8);
        MONTHS.put("sep", 9);
        MONTHS.put("oct", 10);
        MONTHS.put("nov", 11);
        MONTHS.put("dec", 12);
    }

    public enum TimeStatus {
        EARLY, ON_TIME, DELAYED
    }

    private MessageGatewayRepository messageGatewayRepository;
    private NotificationRepository notificationRepository;
    private ParentSubmissionRepository parentSubmissionRepository;
    private ProcessRepository processRepository;
    private ProcessParentChildRepository processParentChildRepository;
    private ProcessStepRepository processStepRepository;
    private ScheduleDefinitionRepository scheduleDefinitionRepository;
    private ScheduledSubmissionRepository scheduledSubmissionRepository;
    private SpotlightEmailService spotlightEmailService;
    private StatusRepository statusRepository;
    private SubmissionRepository submissionRepository;
    private SubmissionStepRepository submissionStepRepository;

    public SubmissionStepService(MessageGatewayRepository messageGatewayRepository,
            NotificationRepository notificationRepository, ParentSubmissionRepository parentSubmissionRepository,
            ProcessRepository processRepository, ProcessParentChildRepository processParentChildRepository,
            ProcessStepRepository processStepRepository, ScheduleDefinitionRepository scheduleDefinitionRepository,
            ScheduledSubmissionRepository scheduledSubmissionRepository, SpotlightEmailService spotlightEmailService,
            StatusRepository statusRepository, SubmissionRepository submissionRepository,
            SubmissionStepRepository submissionStepRepository) {
        this.messageGatewayRepository = messageGatewayRepository;
        this.notificationRepository = notificationRepository;
        this.parentSubmissionRepository = parentSubmissionRepository;
        this.processRepository = processRepository;
        this.processParentChildRepository = processParentChildRepository;
        this.processStepRepository = processStepRepository;
        this.scheduleDefinitionRepository = scheduleDefinitionRepository;
        this.scheduledSubmissionRepository = scheduledSubmissionRepository;
        this.spotlightEmailService = spotlightEmailService;
        this.statusRepository = statusRepository;
        this.submissionRepository = submissionRepository;
        this.submissionStepRepository = submissionStepRepository;
    }

    Optional<SubmissionStep> getLatestOpenSubmissionStepBySubmissionId(Long submissionId, Long stepId) {
        List<SubmissionStep> latestSubmissionSteps = submissionStepRepository
                .findBySubmissionIdAndEndTimeIsNullOrderByStartTimeDesc(submissionId);
        if (latestSubmissionSteps.isEmpty()) {
            return Optional.empty();
        } else {
            SubmissionStep latestSubmissionStep = null;
            for (SubmissionStep submissionStep : latestSubmissionSteps) {
                if (!submissionStep.getProcessStep().getParallel() && latestSubmissionStep == null) {
                    latestSubmissionStep = submissionStep;
                }
                if (submissionStep.getProcessStep().getId().compareTo(stepId) == 0) {
                    return Optional.of(submissionStep);
                }
            }
            return Optional.ofNullable(latestSubmissionStep);
        }
    }

    Optional<User> getAppOwner(Process process) {
        if (process.getAppOwner() != null) {
            return Optional.of(process.getAppOwner());
        } else if (process.getSender() != null && process.getSender().getAppOwner() != null) {
            return Optional.of(process.getSender().getAppOwner());
        } else {
            return Optional.empty();
        }
    }

    void sendEmailToProcessOwner(Long notificationId, SubmissionStep submissionStep, Process process,
            Submission submission, ProcessStep processStep) {
        List<String> emails = new ArrayList<>();
        notificationRepository.findFirstByProcessIdAndSubmissionTypeAndProcessStepIdIsNullAndStatusIdIsNull(
                process.getId(), Constants.REQUIRED_STEPS).ifPresent(notification -> {
                    emails.addAll(notification.getUserMobiles().stream()
                            .map(notificationMobile -> notificationMobile.getUser().getEmail().trim())
                            .collect(Collectors.toList()));
                });
        getAppOwner(process).ifPresent(user -> emails.add(user.getEmail()));
        EmailModel emailModel = new EmailModel(notificationId, String.join(" ", emails), false);
        emailModel.submission = submission;
        emailModel.submissionStep = submissionStep;
        emailModel.processStep = processStep;
        spotlightEmailService.genericSend(emailModel);
    }

    void sendSMS(Notification notification, SubmissionStep submissionStep, ParentSubmission parentSubmission,
            Submission submission, ProcessStep processStep) {
        for (NotificationMobile notificationMobile : notification.getUserMobiles()) {
            User user = notificationMobile.getUser();
            if (user.getCarrier() == null || user.getPhoneNumber() == null) {
                System.out.println("Notification: - " + notification.getId());
                System.out.println("User doesn't have phone and carrier assigned. User: - " + user.getName());
                continue;
            }
            String smsGateway = user.getPhoneNumber() + user.getCarrier().getGateway();
            if (!smsGateway.isEmpty()) {
                EmailModel emailModel = new EmailModel(notification.getNotificationTemplateID(), smsGateway, true);
                emailModel.submission = submission;
                emailModel.submissionStep = submissionStep;
                emailModel.parentSubmission = parentSubmission;
                if (notification.getSubmissionType().equalsIgnoreCase(Constants.REQUIRED_STEPS)) {
                    emailModel.processStep = processStep;
                }
                spotlightEmailService.genericSend(emailModel);
            }
        }
    }

    void sendEmail(Notification notification, SubmissionStep submissionStep, ParentSubmission parentSubmission,
            Process process, Submission submission) {
        String emailList = (notification.getAdditionalEmails().trim() + " " + process.getSupportTeamEmail()).trim();
        if (!emailList.isEmpty()) {
            EmailModel emailModel = new EmailModel(notification.getNotificationTemplateID(), emailList, false);
            emailModel.submission = submission;
            emailModel.submissionStep = submissionStep;
            emailModel.parentSubmission = parentSubmission;
            spotlightEmailService.genericSend(emailModel);
        }
    }

    void getNotificationForRequiredSteps(Submission submission, SubmissionStepRequest submissionStepRequest) {
        if (submissionStepRequest.getProcessStepName().equalsIgnoreCase("end")
                && (submissionStepRequest.getStatus() == null
                        || submissionStepRequest.getStatus().equalsIgnoreCase("success"))) {
            List<SubmissionStep> submissionSteps = submissionStepRepository
                    .findBySubmissionIdOrderByIdAsc(submission.getId());
            List<ProcessStep> processSteps = processStepRepository.findByProcessId(submission.getProcess().getId());
            for (SubmissionStep submissionStep : submissionSteps) {
                if (submissionStep.getProcessStep().getRequired() && submissionStep.getEndTime() == null) {
                    sendEmailToProcessOwner(NotificationTemplate.REQUIRED_STEP_NOTIFICATION, submissionStep,
                            submission.getProcess(), submission, null);
                    notificationRepository
                            .findFirstByProcessIdAndSubmissionTypeAndProcessStepIdIsNullAndStatusIdIsNullAndEnableTextMessagingIsNotNull(
                                    submission.getProcess().getId(), Constants.REQUIRED_STEPS)
                            .ifPresent(notification -> {
                                sendSMS(notification, submissionStep, null, submission, null);
                            });
                }
                processSteps.remove(submissionStep.getProcessStep());
            }
            for (ProcessStep processStep : processSteps) {
                if (processStep.getRequired()) {
                    sendEmailToProcessOwner(NotificationTemplate.SUBMISSION_STEP_INCOMPLETE, null,
                            submission.getProcess(), submission, processStep);
                }
            }
        }
    }

    Optional<ScheduledSubmission> getClosestScheduledSubmission(Long processId, Date systemTime) {
        Calendar nowPlus15Min = Calendar.getInstance();
        nowPlus15Min.setTime(systemTime);
        nowPlus15Min.set(Calendar.MINUTE, nowPlus15Min.get(Calendar.MINUTE) + 15);
        Calendar nowMinus12Hours = Calendar.getInstance();
        nowMinus12Hours.setTime(systemTime);
        nowMinus12Hours.set(Calendar.HOUR, nowMinus12Hours.get(Calendar.HOUR) - 12);
        return scheduledSubmissionRepository
                .findFirstByProcessIdAndStartTimeIsBetweenAndSubmissionIsNullOrderByStartTimeDesc(processId,
                        nowMinus12Hours.getTime(), nowPlus15Min.getTime());
    }

    Optional<ScheduledSubmission> getClosestPredecessorScheduledSubmission(Long processId, Date systemTime) {
        Calendar nowPlus15Min = Calendar.getInstance();
        nowPlus15Min.setTime(systemTime);
        nowPlus15Min.set(Calendar.MINUTE, nowPlus15Min.get(Calendar.MINUTE) + 15);
        return scheduledSubmissionRepository
                .findFirstByProcessIdAndStartTimeIsBetweenAndSubmissionIsNullAndPredecessorSchSubIdIsNotNullOrderByStartTimeDesc(
                        processId, systemTime, nowPlus15Min.getTime());
    }

    ParentSubmission openNewParentSubmission(Submission childSubmission) {
        ParentSubmission newParentSubmission = new ParentSubmission();
        newParentSubmission.setProcess(childSubmission.getProcess().getProcessParent());
        newParentSubmission.setStartTime(childSubmission.getStartTime());
        newParentSubmission.setAdHoc(false);
        if (childSubmission.getStatus().getName().equalsIgnoreCase("success")) {
            newParentSubmission.setStatus(statusRepository.findFirstByName("in progress").get());
        } else {
            newParentSubmission.setStatus(childSubmission.getStatus());
        }
        return newParentSubmission;
    }

    TimeStatus parentTimeBlockStatus(List<Process> childrenProcesses) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH) - 1); // starting yesterday
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        Date start = today.getTime();
        Date now = new Date(); // to right now
        List<Long> processList = childrenProcesses.stream().map(Process::getId).collect(Collectors.toList());
        List<ScheduledSubmission> scheduledSubmissions = scheduledSubmissionRepository
                .findByStartTimeIsBetweenAndProcessIdIsInOrderByStartTimeAsc(start, now, processList);
        TimeStatus status = TimeStatus.ON_TIME;
        for (ScheduledSubmission scheduledSubmission : scheduledSubmissions) {
            status = TimeStatus.EARLY;
            if (scheduledSubmission.getSubmission() != null) {
                if (now.getTime() > (scheduledSubmission.getEndTime().getTime()
                        + scheduledSubmission.getTolerance() * MINUTES_1)) {
                    return TimeStatus.DELAYED;
                }
                if (now.getTime() > scheduledSubmission.getStartTime().getTime()) {
                    status = TimeStatus.ON_TIME;
                }
            }
        }
        return status;
    }

    void updateParentSubmissionStatus(Submission submission) {
        parentSubmissionRepository.findById(submission.getParentId()).ifPresent(parentSubmission -> {
            Set<ChildSubmission> childSubmissions = parentSubmission.getChildren();
            List<Process> childProcesses = processRepository
                    .findByProcessParentId(parentSubmission.getProcess().getId());
            String parentSubmissionStatus = null;
            if (!childSubmissions.isEmpty()) {
                int submittedChildCounter = 0;
                int failedCounter = 0;
                int warningCounter = 0;
                int predecessorNotSubmitted = 0;
                Map<Long, String> submittedProcessMap = new HashMap<>();
                Map<Long, Object> successProcessMap = new HashMap<>();
                for (ChildSubmission childSubmission : childSubmissions) {
                    if (submission.getStatus().getName().equalsIgnoreCase("success")) {
                        successProcessMap.put(submission.getProcess().getId(), submission);
                    }
                    if (!submittedProcessMap.containsKey(childSubmission.getProcess().getId())) {
                        submittedProcessMap.put(childSubmission.getProcess().getId(),
                                childSubmission.getStatus().getName());
                        submittedChildCounter++;
                    }
                    if (!successProcessMap.containsKey(childSubmission.getProcess().getId())
                            && childSubmission.getStatus().getName().equalsIgnoreCase("success")) {
                        successProcessMap.put(childSubmission.getProcess().getId(), childSubmission);
                    }
                }
                for (ChildSubmission childSubmission : childSubmissions) {
                    if (submission.getStatus().getName().equalsIgnoreCase("failed")) {
                        failedCounter++;
                    }
                    if (submission.getStatus().getName().equalsIgnoreCase("warning")) {
                        warningCounter++;
                    }
                    if (childSubmission.getStatus().getName().equalsIgnoreCase("failed")
                            && !successProcessMap.containsKey(childSubmission.getProcess().getId())) {
                        failedCounter++;
                    }
                    if (childSubmission.getStatus().getName().equalsIgnoreCase("warning")
                            && !successProcessMap.containsKey(childSubmission.getProcess().getId())) {
                        warningCounter++;
                    }
                    ProcessParentChild predecessor = processParentChildRepository.findPredecessorByProcessIdAndChildId(
                            childSubmission.getProcess().getProcessParent().getId(),
                            childSubmission.getProcess().getId());
                    // there is one predecessor that has not submitted yet
                    if (predecessor != null && submittedProcessMap.containsKey(predecessor.getProcessChild().getId())) {
                        predecessorNotSubmitted++;
                    }
                }
                parentSubmissionStatus = "in progress";
                // all child Processes have at least one submission with Success, status is
                // Success
                if (childProcesses.size() == successProcessMap.size()) {
                    parentSubmissionStatus = "success";
                    parentSubmission.setEndTime(submission.getEndTime());
                } else if (childProcesses.size() == successProcessMap.size()
                        && submission.getStatus().getName().equalsIgnoreCase("in progress")) {
                    parentSubmissionStatus = "success";
                    // all child processes have submitted one submission and there is one Failed
                    // submission with further no Success, then status Failed
                } else if (childProcesses.size() == submittedChildCounter && failedCounter > 0) {
                    parentSubmissionStatus = "failed";
                    parentSubmission.setEndTime(submission.getEndTime());
                    // all chiild processes have submitted one submission and there is one Warnind
                    // submission with no further Success, then status Warning
                } else if (childProcesses.size() == submittedChildCounter
                        && (warningCounter > 0 || parentTimeBlockStatus(childProcesses) == TimeStatus.DELAYED)) {
                    parentSubmissionStatus = "warning";
                    parentSubmission.setEndTime(submission.getEndTime());
                    // No all child processes have submitted and there is at least a Failure, then
                    // status is Warning.
                } else if (successProcessMap.size() < submittedChildCounter
                        && submission.getStatus().getName().equalsIgnoreCase("failed") || failedCounter > 0) {
                    parentSubmissionStatus = "warning";
                    // No all child processes have submitted and there is at least a Warning (No
                    // failure), then Status is Warning. If child sequence can be ignored, then it
                    // would be In Progress
                } else if (successProcessMap.size() < submittedChildCounter && failedCounter == 0 && (warningCounter > 0
                        || (predecessorNotSubmitted > 0 && !parentSubmission.getProcess().getIgnoreChildSequence()))) {
                    parentSubmissionStatus = "warning";
                } else if (childProcesses.size() > submittedChildCounter && failedCounter == 0 && warningCounter == 0
                        && (predecessorNotSubmitted == 0 || parentSubmission.getProcess().getIgnoreChildSequence())
                        && submission.getStatus().getName().equalsIgnoreCase("success")) {
                    if (parentTimeBlockStatus(childProcesses) == TimeStatus.DELAYED) {
                        parentSubmissionStatus = "warning";
                    } else {
                        parentSubmissionStatus = "in progress";
                    }
                }
            }
            // Set endtime only if this is the last process in Parent
            if (processParentChildRepository.findSuccessorByProcessIdAndChildId(parentSubmission.getProcess().getId(),
                    submission.getProcess().getId()) == null) {
                parentSubmission.setEndTime(submission.getEndTime());
            }
            Status newStatus = statusRepository.findFirstByNameIgnoreCase(parentSubmissionStatus).get();
            parentSubmission.setStatus(newStatus);
            notificationRepository
                    .findFirstByProcessIdAndStatusId(parentSubmission.getProcess().getId(), newStatus.getId())
                    .ifPresent(notification -> {
                        sendEmail(notification, null, parentSubmission, parentSubmission.getProcess(), null);
                    });
            notificationRepository.findFirstByProcessIdAndStatusIdAndEnableTextMessagingIsNotNull(
                    parentSubmission.getProcess().getId(), newStatus.getId()).ifPresent(notification -> {
                        sendSMS(notification, null, parentSubmission, null, null);
                    });
            parentSubmissionRepository.save(parentSubmission);
        });
    }

    void assignParentSubmission(Submission childSubmission, SubmissionStepRequest submissionStepRequest) {
        if (childSubmission.getProcess().getProcessParent() != null) {
            Process parentProcess = childSubmission.getProcess().getProcessParent();
            List<Process> childrenProcesses = processRepository.findByProcessParentId(parentProcess.getId());
            boolean adHoc = parentTimeBlockStatus(childrenProcesses) == TimeStatus.DELAYED;
            if (processParentChildRepository.findSuccessorByProcessIdAndChildId(parentProcess.getId(),
                    childSubmission.getProcess().getId()) == null) {
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DATE, -1);
                Date yesterday = c.getTime();
                Optional<ParentSubmission> lastClosedParentSubmission = parentSubmissionRepository
                        .findFirstByProcessIdAndStartTimeIsGreaterThanOrderByIdDesc(parentProcess.getId(), yesterday);
                if (lastClosedParentSubmission.isPresent()) {
                    childSubmission.setParent(lastClosedParentSubmission.get());
                } else {
                    ParentSubmission newParentSubmission = openNewParentSubmission(childSubmission);
                    newParentSubmission.setAdHoc(adHoc);
                    newParentSubmission = parentSubmissionRepository.save(newParentSubmission);
                    childSubmission.setParent(newParentSubmission);
                }
            } else {
                Optional<ParentSubmission> parentSubmission = parentSubmissionRepository
                        .findFirstByProcessIdAndEndTimeIsNullOrderByStartTimeDesc(parentProcess.getId());
                if (parentSubmission.isEmpty()) {
                    if (processParentChildRepository.findPredecessorByProcessIdAndChildId(parentProcess.getId(),
                            childSubmission.getProcess().getId()) == null) {
                        ParentSubmission newParentSubmission = openNewParentSubmission(childSubmission);
                        newParentSubmission.setAdHoc(adHoc);
                        newParentSubmission = parentSubmissionRepository.save(newParentSubmission);
                        childSubmission.setParent(newParentSubmission);
                    } else {
                        Calendar c = Calendar.getInstance();
                        c.add(Calendar.DATE, -1);
                        Date yesterday = c.getTime();
                        Optional<ParentSubmission> lastParentSubmission = parentSubmissionRepository
                                .findFirstByProcessIdAndStartTimeIsGreaterThanOrderByIdDesc(parentProcess.getId(),
                                        yesterday);
                        if (lastParentSubmission.isPresent()) {
                            childSubmission.setParent(lastParentSubmission.get());
                        } else {
                            ParentSubmission newParentSubmission = openNewParentSubmission(childSubmission);
                            newParentSubmission.setAdHoc(adHoc);
                            newParentSubmission = parentSubmissionRepository.save(newParentSubmission);
                            childSubmission.setParent(newParentSubmission);
                        }
                    }
                } else {
                    Calendar today = Calendar.getInstance();
                    today.set(Calendar.HOUR_OF_DAY, 0);
                    today.set(Calendar.MINUTE, 0);
                    today.set(Calendar.SECOND, 0);
                    today.set(Calendar.MILLISECOND, 0);
                    TimeStatus timeBlockStatus = parentTimeBlockStatus(childrenProcesses);
                    if (timeBlockStatus == TimeStatus.EARLY
                            && (parentSubmission.get().getStartTime().after(today.getTime()))) {
                        childSubmission.setParent(parentSubmission.get());
                        updateParentSubmissionStatus(childSubmission);
                    } else if (timeBlockStatus == TimeStatus.ON_TIME && !parentSubmission.get().getAdHoc()) {
                        childSubmission.setParent(parentSubmission.get());
                        updateParentSubmissionStatus(childSubmission);
                    } else if (timeBlockStatus == TimeStatus.DELAYED
                            && (parentSubmission.get().getStartTime().after(today.getTime()))
                            && (processParentChildRepository.findSuccessorByProcessIdAndChildId(parentProcess.getId(),
                                    childSubmission.getProcess().getId()) == null)
                            && !parentSubmission.get().getAdHoc()) {
                        childSubmission.setParent(parentSubmission.get());
                        updateParentSubmissionStatus(childSubmission);
                    } else if (timeBlockStatus == TimeStatus.DELAYED
                            && (parentSubmission.get().getStartTime().after(today.getTime()))
                            && parentSubmission.get().getAdHoc()) {
                        childSubmission.setParent(parentSubmission.get());
                        updateParentSubmissionStatus(childSubmission);
                    } else {
                        ParentSubmission newParentSubmission = openNewParentSubmission(childSubmission);
                        if (timeBlockStatus != TimeStatus.ON_TIME) {
                            newParentSubmission.setAdHoc(true);
                        }
                        parentSubmissionRepository.save(newParentSubmission);
                        childSubmission.setParent(newParentSubmission);
                    }
                }
            }
        }
    }

    int stringToInt(String number) {
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    void updateSubmissionPeriod(Submission submission, String period) {
        submission.setPeriodYear(0);
        submission.setPeriodQuarter(0);
        submission.setPeriodMonth(0);
        if (period != null && !period.isEmpty()) {
            submission.setPeriod(period);
            switch (period.charAt(0)) {
                case 'Y':
                    if (period.length() >= 5) {
                        submission.setPeriodYear(stringToInt(period.substring(1, 5)));
                    }
                    break;
                case 'Q':
                    if (period.length() >= 5) {
                        submission.setPeriodYear(stringToInt("20" + period.substring(1, 3)));
                        submission.setPeriodQuarter(stringToInt(period.substring(4)));
                    }
                    break;
                case 'M':
                    if (period.length() >= 5 && Character.isDigit(period.charAt(2))) {
                        int month = stringToInt(period.substring(4));
                        submission.setPeriodYear(stringToInt("20" + period.substring(1, 3)));
                        if (month < 4) {
                            submission.setPeriodQuarter(1);
                        } else if (month < 7) {
                            submission.setPeriodQuarter(2);
                        } else if (month < 10) {
                            submission.setPeriodQuarter(3);
                        } else {
                            submission.setPeriodQuarter(4);
                        }
                        submission.setPeriodMonth(month);
                        break;
                    }
                default:
                    if (period.length() >= 6) {
                        String monthString = period.substring(0, 3);
                        int month = MONTHS.getOrDefault(monthString.toLowerCase(), 0);
                        if (month > 0) {
                            submission.setPeriodYear(stringToInt("20" + period.substring(4)));
                            if (month < 4) {
                                submission.setPeriodQuarter(1);
                            } else if (month < 7) {
                                submission.setPeriodQuarter(2);
                            } else if (month < 10) {
                                submission.setPeriodQuarter(3);
                            } else {
                                submission.setPeriodQuarter(4);
                            }
                            submission.setPeriodMonth(month);
                        }
                    }
                    break;
            }
        }
    }

    void autoAcknowledgeSubmission(Submission submission) {
        Date now = new Date();
        submissionRepository
                .findFirstByProcessIdAndIdIsNotOrderByStartTimeDesc(submission.getProcess().getId(), submission.getId())
                .ifPresent(latestSubmission -> {
                    long diff = now.getTime() - latestSubmission.getStartTime().getTime();
                    if (latestSubmission.getStatus().getName().equalsIgnoreCase("failed") && (diff < MINUTES_1 * 60)) {
                        if (submission.getAltId() == null
                                || submission.getAltId().equalsIgnoreCase(latestSubmission.getAltId())) {
                            latestSubmission.setAcknowledgementFlag(true);
                            latestSubmission.setAcknowledgementDate(now);
                            latestSubmission.setAcknowledgementNote(
                                    "Auto Aknowledged by new submission ID: " + submission.getId());
                        }
                    }
                });
    }

    String getAsJSONString(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    SubmissionStep openNewSubmission(Process process, ProcessStep processStep,
            SubmissionStepRequest submissionStepRequest) {
        Long processId = process.getId();
        Submission newSubmission = new Submission();
        newSubmission.setProcess(process);
        newSubmission.setStartTime(submissionStepRequest.getStartTime());
        newSubmission.setNotes(submissionStepRequest.getSubmissionNotes());
        newSubmission.setAltId(submissionStepRequest.getAltSubmissionId());
        newSubmission.setDataFile(submissionStepRequest.getDataFile());
        newSubmission.setReportFile(submissionStepRequest.getReportFile());
        newSubmission.setStatus(statusRepository.findFirstByNameIgnoreCase("in progress").get());
        newSubmission = submissionRepository.save(newSubmission);
        try {
            assignParentSubmission(newSubmission, submissionStepRequest);               
        } catch (Exception e) {
            e.printStackTrace();
            throw new ConflictException("Error while assigning parent submission. " + e.getMessage());
        }        
        newSubmission.setAdHoc(submissionStepRequest.getAdHoc());
        if (!submissionStepRequest.getAdHoc()) {
            Optional<ScheduledSubmission> optionalScheduledSubmission = getClosestScheduledSubmission(processId,
                    submissionStepRequest.getStartTime());
            if (optionalScheduledSubmission.isPresent()) {
                ScheduledSubmission scheduledSubmission = optionalScheduledSubmission.get();
                scheduledSubmission.setSubmission(newSubmission);
                scheduledSubmissionRepository.save(scheduledSubmission);
            } else {
                newSubmission.setAdHoc(true);
            }
        }
        updateSubmissionPeriod(newSubmission, submissionStepRequest.getPeriod());
        autoAcknowledgeSubmission(newSubmission);
        newSubmission.setBu(submissionStepRequest.getBu());
        newSubmission = submissionRepository.save(newSubmission);
        SubmissionStep newSubmissionStep = new SubmissionStep();
        newSubmissionStep.setSubmissionId(newSubmission.getId());
        newSubmissionStep.setProcessStep(processStep);
        newSubmissionStep.setStartTime(submissionStepRequest.getStartTime());
        newSubmissionStep.setEndTime(submissionStepRequest.getEndTime());
        newSubmissionStep.setStatus(statusRepository.findFirstByNameIgnoreCase("success").get());
        newSubmissionStep.setNotes(submissionStepRequest.getStepNotes());
        newSubmissionStep.setRequestPayload(getAsJSONString(submissionStepRequest));
        notificationRepository
                .findFirstByProcessStepIdAndStatusId(processStep.getId(), newSubmissionStep.getStatus().getId())
                .ifPresent(notification -> {
                    sendEmail(notification, newSubmissionStep, null, process, null);
                    sendSMS(notification, newSubmissionStep, null, null, processStep);
                });
        notificationRepository.findFirstByProcessIdAndStatusId(process.getId(), newSubmission.getStatus().getId())
                .ifPresent(notification -> {
                    sendEmail(notification, newSubmissionStep, null, process, null);
                    sendSMS(notification, newSubmissionStep, null, null, processStep);
                });
        return submissionStepRepository.save(newSubmissionStep);
    }

    void closeParallelPendingSteps(Submission submission) {
        List<SubmissionStep> pendingSubmissionSteps = submissionStepRepository
                .findBySubmissionIdAndEndTimeIsNullOrderByStartTimeDesc(submission.getId());
        for (SubmissionStep pendingSubmissionStep : pendingSubmissionSteps) {
            pendingSubmissionStep.setEndTime(submission.getEndTime());
            pendingSubmissionStep.setStatus(statusRepository.findFirstByNameIgnoreCase("success").get());
        }
        submissionStepRepository.saveAll(pendingSubmissionSteps);
    }

    void getNotificationSubmissionStepIncomplete(Submission submission, SubmissionStepRequest submissionStepRequest) {
        List<SubmissionStep> submissionSteps = submissionStepRepository
                .findBySubmissionIdOrderByIdAsc(submission.getId());
        for (SubmissionStep submissionStep : submissionSteps) {
            if (submissionStep.getEndTime() == null
                    && !submissionStepRequest.getProcessStepName().equalsIgnoreCase(ProcessStep.END)) {
                notificationRepository
                        .findFirstByProcessIdAndSubmissionTypeAndProcessStepIdIsNullAndStatusIdIsNull(
                                submission.getProcess().getId(), Constants.SUBMISSION_STEP_INCOMPLETE)
                        .ifPresent(notification -> {
                            sendEmail(notification, submissionStep, null, submission.getProcess(), submission);
                            sendSMS(notification, submissionStep, null, submission, submissionStep.getProcessStep());
                        });
            }
        }
    }

    void predecessorEndTimeScheduleSubmissions(ProcessStep processStep, Submission submission,
            SubmissionStepRequest submissionStepRequest) {
        getClosestPredecessorScheduledSubmission(processStep.getProcessId(), submissionStepRequest.getStartTime())
                .ifPresent(scheduledSubmission -> {
                    ScheduleDefinition scheduleDefinition = scheduleDefinitionRepository
                            .findById(scheduledSubmission.getScheduleDefinitionId())
                            .orElseThrow(() -> new ConflictException(String.format(
                                    "Invalid Request . Schedule Definition with ScheduleDefId : '%s' not found : ",
                                    scheduledSubmission.getScheduleDefinitionId())));
                    Calendar scheduleEndTime = Calendar.getInstance();
                    scheduleEndTime.setTime(submission.getEndTime());
                    scheduleEndTime.set(Calendar.MINUTE,
                            scheduleEndTime.get(Calendar.MINUTE) + scheduleDefinition.getTolerance());
                    scheduleDefinition.setStartTime(submission.getEndTime());
                    scheduleDefinition.setEndTime(scheduleEndTime.getTime());
                    scheduleDefinition.setScheduleStartDate(submission.getStartTime());
                    scheduleDefinition.setScheduleEndDate(scheduleEndTime.getTime());
                    scheduleDefinitionRepository.save(scheduleDefinition);
                    scheduledSubmission.setStartTime(submission.getEndTime());
                    scheduledSubmission.setEndTime(scheduleEndTime.getTime());
                    scheduledSubmissionRepository.save(scheduledSubmission);
                });
    }

    SubmissionStep createNewSubmissionStep(ProcessStep processStep, Submission submission,
            SubmissionStepRequest submissionStepRequest) {
        if (!processStep.getDisabled()) {
            SubmissionStep submissionStep = new SubmissionStep();
            submissionStep.setSubmissionId(submission.getId());
            submissionStep.setProcessStep(processStep);
            submissionStep.setStartTime(submissionStepRequest.getStartTime());
            submissionStep.setNotes(submissionStepRequest.getStepNotes());
            submissionStep.setRequestPayload(getAsJSONString(submissionStepRequest));
            if (submissionStepRequest.getProcessStepName().equalsIgnoreCase(ProcessStep.END)) {
                submission.setEndTime(submissionStepRequest.getEndTime());
                if (submissionStepRequest.getStatus() != null) {
                    submissionStep
                            .setStatus(
                                    statusRepository.findFirstByNameIgnoreCase(submissionStepRequest.getStatus())
                                            .orElseThrow(() -> new ConflictException(String.format(
                                                    "Invalid Request . Status with status_id : '%s' not found : ",
                                                    submissionStepRequest.getStatus()))));
                } else {
                    submissionStep.setStatus(statusRepository.findFirstByNameIgnoreCase("success").get());
                }
                submissionStep.setEndTime(submissionStepRequest.getEndTime());
                if (submission.getProcess().getRequiredStepAlrt()) {
                    getNotificationForRequiredSteps(submission, submissionStepRequest);
                }
                closeParallelPendingSteps(submission);
                predecessorEndTimeScheduleSubmissions(processStep, submission, submissionStepRequest);
                getNotificationSubmissionStepIncomplete(submission, submissionStepRequest);
            } else {
                submissionStep.setStatus(statusRepository.findFirstByNameIgnoreCase("in progress").get());
            }
            submissionRepository.save(submission);
            notificationRepository
                    .findFirstByProcessStepIdAndStatusId(processStep.getId(), submissionStep.getStatus().getId())
                    .ifPresent(notification -> {
                        sendEmail(notification, submissionStep, null, submission.getProcess(), null);
                        sendSMS(notification, submissionStep, null, null, null);
                    });
            return submissionStepRepository.save(submissionStep);
        } else {
            throw new ConflictException(String.format("Invalid Request. Step '%s' is disabled", processStep.getName()));
        }
    }

    void updateSubmissionStatus(SubmissionStepRequest submissionStepRequest, Submission submission,
            SubmissionStep submissionStep) {
        if (submissionStepRequest.getStatus() == null) {
            submissionStepRequest.setStatus("success");
        }
        String submissionStatus = "success";
        if (submissionStepRequest.getProcessStepName().equalsIgnoreCase(ProcessStep.END)) {
            switch (submissionStepRequest.getStatus().toLowerCase()) {
                case "success":
                    for (SubmissionStep step : submission.getSteps()) {
                        if (!step.getProcessStep().getName().equalsIgnoreCase(ProcessStep.END)
                                && step.getStatus().getName().equalsIgnoreCase("warning")
                                || step.getStatus().getName().equalsIgnoreCase("failed")) {
                            submissionStatus = "warning";
                            break;
                        }
                    }
                    break;
                case "failed":
                    submissionStatus = "failed";
                    break;
                case "warning":
                    submissionStatus = "warning";
                    break;
            }
        } else {
            switch (submissionStepRequest.getStatus().toLowerCase()) {
                case "failed":
                    submissionStatus = "failed";
                    break;
                case "warning":
                    submissionStatus = "warning";
                    break;
                default:
                    submissionStatus = "in progress";
                    break;
            }
        }
        Status newStatus = statusRepository.findFirstByNameIgnoreCase(submissionStatus).get();
        submission.setStatus(newStatus);
        submissionRepository.save(submission);
    }

    SubmissionStep updateLatestSubmissionStep(ProcessStep processStep, Submission submission,
            SubmissionStepRequest submissionStepRequest, Optional<Status> stepStatus,
            SubmissionStep latestSubmissionStep) throws JsonMappingException, JsonProcessingException {
        SubmissionStepRequest stepRequest = new ObjectMapper().readValue(latestSubmissionStep.getRequestPayload(),
                SubmissionStepRequest.class);
        if (latestSubmissionStep.getProcessStep().getId().compareTo(processStep.getId()) != 0
                && processStep.getParallel() && !processStep.getName().equalsIgnoreCase(ProcessStep.END)) {
            return createNewSubmissionStep(processStep, submission, submissionStepRequest);
        } else {
            latestSubmissionStep.setEndTime(submissionStepRequest.getEndTime());
            latestSubmissionStep.setNotes(submissionStepRequest.getStepNotes());
            latestSubmissionStep.setRequestPayload(getAsJSONString(submissionStepRequest));
           if (stepStatus.isPresent()) {
                latestSubmissionStep.setStatus(stepStatus.get());
            } else {
                latestSubmissionStep.setStatus(statusRepository.findFirstByNameIgnoreCase("success").get());
            }
            /*
             * if (stepRequest.getStatus() != null) { latestSubmissionStep
             * .setStatus(statusRepository.findFirstByNameIgnoreCase(stepRequest.getStatus()
             * ).get()); } else {
             * latestSubmissionStep.setStatus(statusRepository.findFirstByNameIgnoreCase(
             * "success").get()); }
             */
            SubmissionStep submissionStep;
            if (latestSubmissionStep.getProcessStep().getId().compareTo(processStep.getId()) == 0) {
                submissionRepository.save(submission);
                updateSubmissionStatus(submissionStepRequest, submission, latestSubmissionStep);
                updateParentSubmissionStatus(submission);
                submissionStep = submissionStepRepository.save(latestSubmissionStep);
            } else {
                submissionStepRepository.save(latestSubmissionStep);
                submissionStep = createNewSubmissionStep(processStep, submission, submissionStepRequest);
            }
            notificationRepository
                    .findFirstByProcessStepIdAndStatusId(processStep.getId(), latestSubmissionStep.getStatus().getId())
                    .ifPresent(notification -> {
                        sendEmail(notification, latestSubmissionStep, null, submission.getProcess(), null);
                        sendSMS(notification, latestSubmissionStep, null, null, null);
                    });
            return submissionStep;
        }
    }

    Submission getSubmission(SubmissionStepRequest submissionStepRequest, Process process) {
        if (submissionStepRequest.getSubmissionId() != null) {
            return submissionRepository.findById(submissionStepRequest.getSubmissionId())
                    .orElseThrow(() -> new NotFoundException(
                            String.format("Invalid Request . Submission with submissionId : '%s' not found : ",
                                    submissionStepRequest.getSubmissionId())));
        } else if (submissionStepRequest.getAltSubmissionId() != null) {
            return submissionRepository.findFirstByProcessIdAndAltIdIgnoreCaseAndEndTimeIsNullOrderByIdDesc(
                    process.getId(), submissionStepRequest.getAltSubmissionId())
                                        .orElseThrow((()-> new NotFoundException(String.format("No open submission was found for alt id '%s'", submissionStepRequest.getAltSubmissionId()))));
        } else {
            return submissionRepository.findFirstByProcessIdAndEndTimeIsNullOrderByStartTimeDesc(process.getId())
                                        .orElseThrow(()-> new NotFoundException(String.format("No open submission was found for '%s' process", process.getName())));
        }        
    }

    SubmissionStep updateSubmissionAndStep(ProcessStep processStep, Submission submission,
            SubmissionStepRequest submissionStepRequest, Optional<Status> stepStatus)
            throws JsonMappingException, JsonProcessingException {
        submission.setNotes(submissionStepRequest.getSubmissionNotes());
        submission.setDataFile(submissionStepRequest.getDataFile());
        submission.setReportFile(submissionStepRequest.getReportFile());
        if (submissionStepRequest.getRecords() != null)
            submission.setRecords(submissionStepRequest.getRecords());
        if (submissionStepRequest.getWarnings() != null)
            submission.setWarnings(submissionStepRequest.getWarnings());
        if (submissionStepRequest.getErrors() != null)
            submission.setErrors(submissionStepRequest.getErrors());
        updateSubmissionPeriod(submission, submissionStepRequest.getPeriod());
        Optional<SubmissionStep> latestSubmissionStepOptional = getLatestOpenSubmissionStepBySubmissionId(
                submission.getId(), processStep.getId());
        SubmissionStep submissionStep = latestSubmissionStepOptional.isPresent()
                ? updateLatestSubmissionStep(processStep, submission, submissionStepRequest, stepStatus,
                        latestSubmissionStepOptional.get())
                : createNewSubmissionStep(processStep, submission, submissionStepRequest);
                
        updateSubmissionStatus(submissionStepRequest, submission, submissionStep);

        try {
            updateParentSubmissionStatus(submission);            
        } catch (Exception e) {
            e.printStackTrace();
            throw new ConflictException("Error while updating parent submission. " + e.getMessage());
        }
        
        notificationRepository
                .findFirstByProcessIdAndStatusId(submission.getProcess().getId(), submission.getStatus().getId())
                .ifPresent(notification -> {
                    sendEmail(notification, submissionStep, null, submission.getProcess(), submission);
                    sendSMS(notification, submissionStep, null, submission, null);
                });
        return submissionStep;
    }

    @Transactional
    public SubmissionStep create(Long processId, SubmissionStepRequest submissionStepRequest)
            throws JsonMappingException, JsonProcessingException {
        Process process = processRepository.findById(processId)
                                            .orElseThrow(() -> new NotFoundException(String.format("Invalid Request . Process with processId : '%s' not found : ", processId)));
        if (process.getApproved() != 'A') {
            throw new ConflictException("Process is not approved. Please contact Spotlight team.");
        }
        ProcessStep processStep = processStepRepository
                .findFirstByProcessIdAndNameIgnoreCase(processId, submissionStepRequest.getProcessStepName())
                .orElseThrow(()-> new NotFoundException(String.format("Step '%s' not found in process definition", submissionStepRequest.getProcessStepName())));
        Optional<Status> stepStatus = Optional.empty();
        if (submissionStepRequest.getStatus() != null) {
            Status status = statusRepository.findFirstByNameIgnoreCase(submissionStepRequest.getStatus())
                    .orElseThrow(() -> new NotFoundException(String.format("'%s' is not a valid status", submissionStepRequest.getStatus())));
            stepStatus = Optional.of(status);
        }
        if (submissionStepRequest.getProcessStepName().equalsIgnoreCase(ProcessStep.START)) {
            return openNewSubmission(process, processStep, submissionStepRequest);
        } else {
            Submission submission = getSubmission(submissionStepRequest, process);
            return updateSubmissionAndStep(processStep, submission, submissionStepRequest, stepStatus);
        }
    }

}
