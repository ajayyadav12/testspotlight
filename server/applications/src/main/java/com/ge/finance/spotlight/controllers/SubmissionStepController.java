package com.ge.finance.spotlight.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.validation.Valid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ge.finance.spotlight.exceptions.NotFoundException;
import com.ge.finance.spotlight.models.ChildSubmission;
import com.ge.finance.spotlight.models.MessageGateway;
import com.ge.finance.spotlight.models.Notification;
import com.ge.finance.spotlight.models.NotificationTemplate;
import com.ge.finance.spotlight.models.ParentSubmission;
import com.ge.finance.spotlight.models.Process;
import com.ge.finance.spotlight.models.ProcessStep;
import com.ge.finance.spotlight.models.ScheduledSubmission;
import com.ge.finance.spotlight.models.Status;
import com.ge.finance.spotlight.models.Submission;
import com.ge.finance.spotlight.models.SubmissionStep;
import com.ge.finance.spotlight.models.User;
import com.ge.finance.spotlight.repositories.MessageGatewayRepository;
import com.ge.finance.spotlight.repositories.NotificationRepository;
import com.ge.finance.spotlight.repositories.ParentSubmissionRepository;
import com.ge.finance.spotlight.repositories.ProcessRepository;
import com.ge.finance.spotlight.repositories.ProcessStepRepository;
import com.ge.finance.spotlight.repositories.ScheduledSubmissionRepository;
import com.ge.finance.spotlight.repositories.StatusRepository;
import com.ge.finance.spotlight.repositories.SubmissionRepository;
import com.ge.finance.spotlight.repositories.SubmissionStepRepository;
import com.ge.finance.spotlight.repositories.UserRepository;
import com.ge.finance.spotlight.requests.SubmissionStepRequest;
import com.ge.finance.spotlight.services.SpotlightEmailService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/submissions/steps")
public class SubmissionStepController {
    private static final long MINUTES_1 = 60_000;

    private Date systemTime;

    private SubmissionRepository submissionRepository;
    private SubmissionStepRepository submissionStepRepository;
    private ParentSubmissionRepository parentSubmissionRepository;
    private ProcessRepository processRepository;
    private ProcessStepRepository processStepRepository;
    private StatusRepository statusRepository;
    private NotificationRepository notificationRepository;
    private ScheduledSubmissionRepository scheduledSubmissionRepository;
    private SpotlightEmailService spotlightEmailService;
    private UserRepository userRepository;
    private MessageGatewayRepository messageGatewayRepository;

    public SubmissionStepController(SubmissionRepository submissionRepository,
            SubmissionStepRepository submissionStepRepository, ParentSubmissionRepository parentSubmissionRepository,
            ProcessRepository processRepository, ProcessStepRepository processStepRepository,
            StatusRepository statusRepository, NotificationRepository notificationRepository,
            ScheduledSubmissionRepository scheduledSubmissionRepository, SpotlightEmailService spotlightEmailService,
            UserRepository userRepository, MessageGatewayRepository messageGatewayRepository) {
        this.submissionRepository = submissionRepository;
        this.submissionStepRepository = submissionStepRepository;
        this.parentSubmissionRepository = parentSubmissionRepository;
        this.processRepository = processRepository;
        this.processStepRepository = processStepRepository;
        this.statusRepository = statusRepository;
        this.notificationRepository = notificationRepository;
        this.scheduledSubmissionRepository = scheduledSubmissionRepository;
        this.spotlightEmailService = spotlightEmailService;
        this.userRepository = userRepository;
        this.messageGatewayRepository = messageGatewayRepository;
    }

    private Optional<Status> getStatusByName(String name) {
        List<Status> status = statusRepository.findByNameIgnoreCase(name);
        if (status.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(status.get(0));
        }
    }

    private Optional<ParentSubmission> getLatestOpenParentSubmissionByProcessId(Long processId) {
        List<ParentSubmission> latestParentSubmissions = parentSubmissionRepository
                .findByProcessIdAndEndTimeIsNullOrderByStartTimeDesc(processId);
        if (latestParentSubmissions.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(latestParentSubmissions.get(0));
        }
    }

    private Optional<ParentSubmission> getLatestParentSubmissionByProcessId(Long processId, Date yesterday) {
        List<ParentSubmission> latestParentSubmissions = parentSubmissionRepository
                .findByProcessIdAndStartTimeIsGreaterThanOrderByIdDesc(processId, yesterday);
        if (latestParentSubmissions.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(latestParentSubmissions.get(0));
        }
    }

    private Optional<Submission> getLatestOpenSubmissionByProcessId(Long processId) {
        List<Submission> latestSubmissions = submissionRepository
                .findByProcessIdAndEndTimeIsNullOrderByStartTimeDesc(processId);
        if (latestSubmissions.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(latestSubmissions.get(0));
        }
    }

    private Optional<SubmissionStep> getLatestOpenSubmissionStepBySubmissionId(Long submissionId, Long stepId) {
        List<SubmissionStep> latestSubmissionSteps = submissionStepRepository
                .findBySubmissionIdAndEndTimeIsNullOrderByStartTimeDesc(submissionId);
        SubmissionStep latestSubmissionStep = null;
        if (latestSubmissionSteps.isEmpty()) {
            return Optional.empty();
        } else {
            Iterator<SubmissionStep> it = latestSubmissionSteps.iterator();

            while (it.hasNext()) {
                SubmissionStep submissionStep = it.next();

                if (!submissionStep.getProcessStep().getParallel() && latestSubmissionStep == null) {
                    latestSubmissionStep = submissionStep;
                }

                if (submissionStep.getProcessStep().getId().compareTo(stepId) == 0) {
                    return Optional.of(submissionStep);
                }
            }

            if (latestSubmissionStep != null) {
                return Optional.of(latestSubmissionStep);
            } else {
                return Optional.empty();
            }

        }
    }

    private Optional<ProcessStep> getProcessStepByProcessIdAndName(Long processId, String name) {
        List<ProcessStep> processSteps = processStepRepository.findByProcessIdAndNameIgnoreCase(processId, name);
        if (processSteps.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(processSteps.get(0));
        }
    }

    private Optional<Notification> getNotificationForProcessStepAndStatus(Long processStepId, Long statusId) {
        List<Notification> notifications = notificationRepository.findByProcessStepIdAndStatusId(processStepId,
                statusId);
        if (notifications.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(notifications.get(0));
        }

    }

    private Optional<List<Notification>> getSMSNotificationForProcessStepAndStatus(Long processStepId, Long statusId) {
        List<Notification> notifications = notificationRepository
                .findByProcessStepIdAndStatusIdAndEnableTextMessagingIsNotNull(processStepId, statusId);
        if (notifications.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(notifications);
        }

    }

    private Optional<Notification> getNotificationForProcessAndStatus(Long processId, Long statusId) {
        List<Notification> notifications = notificationRepository.findByProcessIdAndStatusId(processId, statusId);
        if (notifications.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(notifications.get(0));
        }
    }

    private Optional<List<Notification>> getSMSNotificationForProcessAndStatus(Long processId, Long statusId) {
        List<Notification> notifications = notificationRepository
                .findByProcessIdAndStatusIdAndEnableTextMessagingIsNotNull(processId, statusId);
        if (notifications.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(notifications);
        }
    }

    private void getNotificationForRequiredSteps(Submission submission, SubmissionStepRequest submissionStepRequest) {
        Set<SubmissionStep> reqSteps = null;
        if (submissionStepRequest.getProcessStepName().equalsIgnoreCase("end")
                && (submissionStepRequest.getStatus() == null || submissionStepRequest.getStatus().equalsIgnoreCase("success"))) {
            List<SubmissionStep> submissionSteps = submissionStepRepository
                    .findBySubmissionIdOrderByIdAsc(submission.getId());
            List<ProcessStep> processSteps = processStepRepository.findByProcessId(submission.getProcess().getId());
            if (!submissionSteps.isEmpty()) {
                Iterator<SubmissionStep> it = submissionSteps.iterator();
                while (it.hasNext()) {
                    SubmissionStep subStep = it.next();
                    if (subStep.getProcessStep().getRequired() && subStep.getEndTime() == null) {
                        sendEmailToProcessOwner(NotificationTemplate.REQUIRED_STEP_NOTIFICATION, subStep, null,
                                submission.getProcess(), submission, null);
                    }
                    if (processSteps.contains(subStep.getProcessStep())) {
                        processSteps.remove(subStep.getProcessStep());
                    }
                }
            }
            if (!processSteps.isEmpty()) {
                Iterator<ProcessStep> psit = processSteps.iterator();
                while (psit.hasNext()) {
                    ProcessStep processStep = psit.next();
                    if (processStep.getRequired()) {
                        sendEmailToProcessOwner(NotificationTemplate.SUBMISSION_STEP_INCOMPLETE, null, null,
                                submission.getProcess(), submission, processStep);
                    }
                }
            }
        }
    }

    private Optional<ScheduledSubmission> getClosestScheduledSubmission(Long processId) {
        Calendar nowPlus15Min = Calendar.getInstance();
        nowPlus15Min.setTime(this.systemTime);
        nowPlus15Min.set(Calendar.MINUTE, nowPlus15Min.get(Calendar.MINUTE) + 15);
        List<ScheduledSubmission> scheduledSubmissions = scheduledSubmissionRepository
                .findByProcessIdAndStartTimeIsLessThanAndSubmissionIsNullOrderByStartTimeDesc(processId,
                        nowPlus15Min.getTime());
        if (scheduledSubmissions.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(scheduledSubmissions.get(0));
        }
    }

    private void sendEmail(Notification notification, SubmissionStep submissionStep, ParentSubmission parentSubmission,
            Process process, Submission submission) {
        String emailList = (notification.getAdditionalEmails().trim() + " " + process.getSupportTeamEmail()).trim();
        if (!emailList.isEmpty()) {
            spotlightEmailService.genericSend(notification.getNotificationTemplateID(), emailList, null, submission,
                    null, null, submissionStep, parentSubmission, null);
        }
    }

    private void sendEmailToProcessOwner(Long notificationId, SubmissionStep submissionStep,
            ParentSubmission parentSubmission, Process process, Submission submission, ProcessStep processStep) {

        User appOwner = getAppOwner(process);
        String emailList = (appOwner != null) ? (appOwner.getEmail()) : "";

        spotlightEmailService.genericSend(notificationId, emailList, null, submission, null, null, submissionStep,
                parentSubmission, processStep);
    }

    private void sendSMS(List<Notification> notificationList, SubmissionStep submissionStep,
            ParentSubmission parentSubmission, Process process, Submission submission) {
        for (Notification notification : notificationList) {
            if (notification.getCreatedFor() != null) {
                User user = userRepository.findFirstBySso(notification.getCreatedFor().getSso());
                MessageGateway messageGateway = messageGatewayRepository.findById(user.getCarrier().getId())
                        .orElseThrow(NotFoundException::new);
                String smsGateway = notification.getCreatedFor().getPhoneNumber() + messageGateway.getGateway();
                if (!smsGateway.isEmpty()) {
                    spotlightEmailService.genericSMSSend(notification.getNotificationTemplateID(), smsGateway, null,
                            submission, null, null, submissionStep, parentSubmission);
                }
                if (!notification.getAdditionalEmails().isEmpty()) {
                    spotlightEmailService.genericSend(notification.getNotificationTemplateID(),
                            notification.getAdditionalEmails().trim(), null, submission, null, null, submissionStep,
                            parentSubmission, null);
                }
            }
        }
    }

    private ParentSubmission openNewParentSubmission(Submission childSubmission) {
        ParentSubmission newParentSubmission = new ParentSubmission();
        Status parentStatus = null;
        newParentSubmission.setProcess(childSubmission.getProcess().getProcessParent());
        newParentSubmission.setStartTime(childSubmission.getStartTime());
        newParentSubmission.setUpdateTime(childSubmission.getStartTime());
        newParentSubmission.setAdHoc(false);
        if (childSubmission.getStatus().getName().equalsIgnoreCase("success")) {
            parentStatus = getStatusByName("in progress").get();
        } else {
            parentStatus = childSubmission.getStatus();
        }
        newParentSubmission.setStatus(parentStatus);

        return newParentSubmission;
    }

    private void assignParentSubmission(Submission childSubmission, SubmissionStepRequest submissionStepRequest) {
        // Based on what we should we create the Parents? Day / Week / Month / Quarter /
        // Period?
        Process parentProcess = childSubmission.getProcess().getProcessParent();
        List<Process> childrenProcesses = null;

        if (parentProcess != null) {
            boolean adHoc = false;
            childrenProcesses = processRepository.findByProcessParentId(parentProcess.getId());
            if (parentTimeBlockStatus(childrenProcesses).equalsIgnoreCase("delayed")) {
                adHoc = true;
            }

            if (childSubmission.getProcess().getSuccessor() == null) {
                Calendar c = new GregorianCalendar();
                c.add(Calendar.DATE, -1);
                Date yesterday = c.getTime();

                // Obtain the latest Parent Submission even if it is clomplete.
                Optional<ParentSubmission> lastClosedParentSubmission = getLatestParentSubmissionByProcessId(
                        parentProcess.getId(), yesterday);
                if (lastClosedParentSubmission.isPresent()) {
                    childSubmission.setParent(lastClosedParentSubmission.get());
                } else {
                    ParentSubmission newParentSubmission = openNewParentSubmission(childSubmission);
                    newParentSubmission.setAdHoc(adHoc);

                    parentSubmissionRepository.save(newParentSubmission);
                    childSubmission.setParent(newParentSubmission);
                }
            } else {
                Optional<ParentSubmission> parentSubmission = getLatestOpenParentSubmissionByProcessId(
                        parentProcess.getId());

                if (!parentSubmission.isPresent()) {

                    // if there is no parent open and it is the first step, create one.
                    if (childSubmission.getProcess().getPredecessor() == null) {
                        ParentSubmission newParentSubmission = openNewParentSubmission(childSubmission);
                        newParentSubmission.setAdHoc(adHoc);

                        parentSubmissionRepository.save(newParentSubmission);
                        childSubmission.setParent(newParentSubmission);
                    } else {
                        Calendar c = new GregorianCalendar();
                        c.add(Calendar.DATE, -1);
                        Date yesterday = c.getTime();

                        Optional<ParentSubmission> lastParentSubmission = getLatestParentSubmissionByProcessId(
                                parentProcess.getId(), yesterday);
                        if (lastParentSubmission.isPresent()) {
                            childSubmission.setParent(lastParentSubmission.get());
                        } else {
                            ParentSubmission newParentSubmission = openNewParentSubmission(childSubmission);
                            newParentSubmission.setAdHoc(adHoc);

                            parentSubmissionRepository.save(newParentSubmission);
                            childSubmission.setParent(newParentSubmission);
                        }
                    }
                } else {
                    Calendar today = Calendar.getInstance();
                    today.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH)); // starting yesterday
                    today.set(Calendar.HOUR_OF_DAY, 0);
                    today.set(Calendar.MINUTE, 0);
                    today.set(Calendar.SECOND, 0);
                    today.set(Calendar.MILLISECOND, 0);

                    String timeBlockStatus = parentTimeBlockStatus(childrenProcesses);
                    // if it is before schedule and there is no parent in the same day create it
                    // otherwise use it
                    // if it is in the schedule and there is no parent for the schedule create it
                    // otherwise use it
                    // if it is delayed in the same day and started in the schedule and there is
                    // children process is the first one create a new onethere is no parent create
                    // it oth

                    if (timeBlockStatus.equalsIgnoreCase("early")
                            && (parentSubmission.get().getStartTime().after(today.getTime()))) {

                        childSubmission.setParent(parentSubmission.get());
                        updateParentSubmissionStatus(submissionStepRequest, childSubmission);

                    } else if (timeBlockStatus.equalsIgnoreCase("on time")
                            && parentSubmission.get().getAdHoc() == false) {
                        childSubmission.setParent(parentSubmission.get());
                        updateParentSubmissionStatus(submissionStepRequest, childSubmission);

                    } else if (timeBlockStatus.equalsIgnoreCase("delayed")
                            && (parentSubmission.get().getStartTime().after(today.getTime()))
                            && (childSubmission.getProcess().getSuccessor() == null)
                            && parentSubmission.get().getAdHoc() == false) {

                        childSubmission.setParent(parentSubmission.get());
                        updateParentSubmissionStatus(submissionStepRequest, childSubmission);
                    } else if (timeBlockStatus.equalsIgnoreCase("delayed")
                            && (parentSubmission.get().getStartTime().after(today.getTime()))
                            && parentSubmission.get().getAdHoc() == true) {

                        childSubmission.setParent(parentSubmission.get());
                        updateParentSubmissionStatus(submissionStepRequest, childSubmission);
                    } else {
                        ParentSubmission newParentSubmission = openNewParentSubmission(childSubmission);
                        if (!timeBlockStatus.equalsIgnoreCase("on time")) {
                            newParentSubmission.setAdHoc(true);
                        }

                        parentSubmissionRepository.save(newParentSubmission);
                        childSubmission.setParent(newParentSubmission);
                    }
                }

            }

        }
    }
    
    private SubmissionStep openNewSubmission(Long processId, ProcessStep processStep,
            SubmissionStepRequest submissionStepRequest) {
        Process process = processRepository.findById(processId).orElseThrow(NotFoundException::new);
        Submission newSubmission = new Submission();
        newSubmission.setProcess(process);
        newSubmission.setStartTime(this.systemTime);
        newSubmission.setUpdateTime(this.systemTime);
        newSubmission.setNotes(submissionStepRequest.getSubmissionNotes());
        newSubmission.setAltId(submissionStepRequest.getAltSubmissionId());

        Status newStatus = getStatusByName("in progress").get();
        newSubmission.setStatus(newStatus);
        newSubmission = submissionRepository.save(newSubmission);

        // Update parent submission with new status
        assignParentSubmission(newSubmission, submissionStepRequest);

        newSubmission.setAdHoc(submissionStepRequest.getAdHoc());

        if (!submissionStepRequest.getAdHoc()) {

            Optional<ScheduledSubmission> scheduledSubmissionOpt = getClosestScheduledSubmission(processId);
            if (scheduledSubmissionOpt.isPresent()) {
                ScheduledSubmission scheduledSubmission = scheduledSubmissionOpt.get();
                scheduledSubmission.setSubmission(newSubmission);
                scheduledSubmissionRepository.save(scheduledSubmission);
            } else {
                newSubmission.setAdHoc(true);
            }

        }

        updateSubmissionPeriod(newSubmission, submissionStepRequest.getPeriod());

        autoAcknowledgeSubmission(newSubmission);

        newSubmission.setRecords(0);
        newSubmission.setWarnings(0);
        newSubmission.setErrors(0);
        newSubmission.setBu(submissionStepRequest.getBu());
        newSubmission = submissionRepository.save(newSubmission);        
        SubmissionStep submissionStep = new SubmissionStep();
        submissionStep.setSubmissionId(submissionStepRequest.getSubmissionId());
        submissionStep.setProcessStep(processStep);
        submissionStep.setSubmissionId(newSubmission.getId());
        submissionStep.setStartTime(this.systemTime);
        submissionStep.setEndTime(this.systemTime);
        submissionStep.setStatus(getStatusByName("success").get());
        submissionStep.setNotes(submissionStepRequest.getStepNotes());

        submissionStep.setRequestPayload(getAsJSONString(submissionStepRequest));
        getNotificationForProcessStepAndStatus(processStep.getId(), submissionStep.getStatus().getId())
                .ifPresent(not -> sendEmail(not, submissionStep, null, process, null));
        getSMSNotificationForProcessStepAndStatus(processStep.getId(), submissionStep.getStatus().getId())
                .ifPresent(not -> sendSMS(not, submissionStep, null, process, null));
        return submissionStepRepository.save(submissionStep);
    }

    private String getAsJSONString(Object obj) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    /**
     * Parallel Steps Logic if request is for the same step then close it. if
     * request is for new step and it is not parallel to the previous step, then
     * closes the latest and open a new step if request is for new step and it is
     * parallel to the previous step, then only creates a new step but it doesn't
     * close the previous one.
     * 
     * @param processStep
     * @param submission
     * @param submissionStepRequest
     * @param stepStatus
     * @param latestSubmissionStep
     * @return
     */
    private SubmissionStep updateLatestSubmissionStep(ProcessStep processStep, Submission submission,
            SubmissionStepRequest submissionStepRequest, Optional<Status> stepStatus,
            SubmissionStep latestSubmissionStep) {

        if (latestSubmissionStep.getProcessStep().getId().compareTo(processStep.getId()) != 0
                && processStep.getParallel() && (!processStep.getName().equalsIgnoreCase(ProcessStep.END))) {
            return createNewSubmissionStep(processStep, submission, submissionStepRequest);
        } else {
            latestSubmissionStep.setEndTime(this.systemTime);
            latestSubmissionStep.setNotes(submissionStepRequest.getStepNotes());
            latestSubmissionStep.setRequestPayload(getAsJSONString(submissionStepRequest));
            if (stepStatus.isPresent()) {
                latestSubmissionStep.setStatus(stepStatus.get());
            } else {
                latestSubmissionStep.setStatus(getStatusByName("success").get());
            }
            if (latestSubmissionStep.getProcessStep().getId() == processStep.getId()) {
                submissionRepository.save(submission);
                updateSubmissionstatus(submissionStepRequest, submission);
                updateParentSubmissionStatus(submissionStepRequest, submission);

                getNotificationForProcessStepAndStatus(processStep.getId(), latestSubmissionStep.getStatus().getId())
                        .ifPresent(not -> sendEmail(not, latestSubmissionStep, null, submission.getProcess(), null));
                getSMSNotificationForProcessStepAndStatus(processStep.getId(), latestSubmissionStep.getStatus().getId())
                        .ifPresent(not -> sendSMS(not, latestSubmissionStep, null, submission.getProcess(), null));
                return submissionStepRepository.save(latestSubmissionStep);
            } else {
                getNotificationForProcessStepAndStatus(processStep.getId(), latestSubmissionStep.getStatus().getId())
                        .ifPresent(not -> sendEmail(not, latestSubmissionStep, null, submission.getProcess(), null));
                getSMSNotificationForProcessStepAndStatus(processStep.getId(), latestSubmissionStep.getStatus().getId())
                        .ifPresent(not -> sendSMS(not, latestSubmissionStep, null, submission.getProcess(), null));
                submissionStepRepository.save(latestSubmissionStep);
                return createNewSubmissionStep(processStep, submission, submissionStepRequest);
            }

        }
    }

    private SubmissionStep createNewSubmissionStep(ProcessStep processStep, Submission submission,
            SubmissionStepRequest submissionStepRequest) {
        SubmissionStep submissionStep = new SubmissionStep();
        submissionStep.setSubmissionId(submission.getId());
        submissionStep.setProcessStep(processStep);
        submissionStep.setStartTime(this.systemTime);
        submissionStep.setNotes(submissionStepRequest.getStepNotes());
        submissionStep.setRequestPayload(getAsJSONString(submissionStepRequest));
        if (submissionStepRequest.getProcessStepName().equalsIgnoreCase(ProcessStep.END)) {
            submission.setEndTime(this.systemTime);
            if (submissionStepRequest.getStatus() != null) {
                submissionStep.setStatus(getStatusByName(submissionStepRequest.getStatus()).get());
            } else {
                submissionStep.setStatus(getStatusByName("success").get());
            }
            submissionStep.setEndTime(this.systemTime);
            getNotificationForRequiredSteps(submission, submissionStepRequest);
            closeParallelPendingSteps(submission);
        } else {
            submissionStep.setStatus(getStatusByName("in progress").get());
        }
        submissionRepository.save(submission);
        getNotificationForProcessStepAndStatus(processStep.getId(), submissionStep.getStatus().getId())
                .ifPresent(not -> sendEmail(not, submissionStep, null, submission.getProcess(), null));
        getSMSNotificationForProcessStepAndStatus(processStep.getId(), submissionStep.getStatus().getId())
                .ifPresent(not -> sendSMS(not, submissionStep, null, submission.getProcess(), null));
        return submissionStepRepository.save(submissionStep);
    }
    
    private SubmissionStep updateSubmissionAndStep(ProcessStep processStep, Submission submission,
            SubmissionStepRequest submissionStepRequest, Optional<Status> stepStatus) {
        submission.setUpdateTime(this.systemTime);
        submission.setNotes(submissionStepRequest.getSubmissionNotes());
        if (submissionStepRequest.getRecords() != null) {
            submission.setRecords(submissionStepRequest.getRecords());
        }
        if (submissionStepRequest.getWarnings() != null) {
            submission.setWarnings(submissionStepRequest.getWarnings());
        }
        if (submissionStepRequest.getErrors() != null) {
            submission.setErrors(submissionStepRequest.getErrors());
        }
        updateSubmissionPeriod(submission, submissionStepRequest.getPeriod());

        // Determines if step is complete. If not closes the current step otherwise
        // opens a new step
        Optional<SubmissionStep> latestSubmissionStepOptional = getLatestOpenSubmissionStepBySubmissionId(
                submission.getId(), processStep.getId());

        SubmissionStep sStep = (latestSubmissionStepOptional.isPresent())
                ? updateLatestSubmissionStep(processStep, submission, submissionStepRequest, stepStatus,
                        latestSubmissionStepOptional.get())
                : createNewSubmissionStep(processStep, submission, submissionStepRequest);

        updateSubmissionstatus(submissionStepRequest, submission);
        updateParentSubmissionStatus(submissionStepRequest, submission);
        return sStep;
    }

    private void updateParentSubmissionStatus(SubmissionStepRequest submissionStepRequest, Submission submission) {
        // This will update status for the inmediate Parent for the current submission
        // If any child process fails --> Failed
        // If any process is in warning --> Warning
        // If anything is in progress or is not started, all succeeding processes should
        // be not started --> Warning
        // All child processes Not Started --> Not Started
        // All child processes Success --> Success
        // Any other scenario --> In Progress

        Set<ChildSubmission> childrenSubmissions = null;
        List<Process> childrenProcesses = null;
        String parentSubmissionStatus = null;
        // Get parent
        Optional<ParentSubmission> optParentSubmission = parentSubmissionRepository.findById(submission.getParentId());

        if (optParentSubmission.isPresent()) {
            ParentSubmission parentSubmission = optParentSubmission.get();
            childrenSubmissions = parentSubmission.getChildren();
            childrenProcesses = processRepository.findByProcessParentId(parentSubmission.getProcess().getId());
            if (!childrenSubmissions.isEmpty()) {

                if (childrenSubmissions.size() == 1) {
                    if (submission.getStatus().getName().equalsIgnoreCase("success") || submission.getStatus().getName()
                            .equalsIgnoreCase("in progress")/* ) && (is not delayed) */) {
                        parentSubmissionStatus = "in progress";
                    } else {
                        parentSubmissionStatus = "warning";
                    }

                } else {

                    // How can I get
                    Iterator<ChildSubmission> it = childrenSubmissions.iterator();
                    Iterator<ChildSubmission> itWarning = childrenSubmissions.iterator();
                    Integer submitted_child_counter = 0;
                    Integer failed_counter = 0;
                    Integer warning_counter = 0;
                    Integer predessesor_not_submitted = 0;
                    HashMap<Long, String> submittedProcessMap = new HashMap<>();
                    HashMap<Long, Object> successProcessMap = new HashMap<>();

                    // Obtain the number of processes in the parent submission.
                    while (it.hasNext()) {
                        if (submission.getStatus().getName().equalsIgnoreCase("success")) {
                            successProcessMap.put(submission.getProcess().getId(), submission.getStatus().getName());
                        }
                        ChildSubmission childSubmission = it.next();

                        if (!submittedProcessMap.containsKey(childSubmission.getProcess().getId())) {
                            // Obtain the Id of submitted processes - Total
                            submittedProcessMap.put(childSubmission.getProcess().getId(),
                                    childSubmission.getStatus().getName());
                            submitted_child_counter++;

                        }

                        // Obtain the number of submitted processes with one Success
                        if (!successProcessMap.containsKey(childSubmission.getProcess().getId())
                                && (childSubmission.getStatus().getName().equalsIgnoreCase("success"))) {
                            successProcessMap.put(childSubmission.getProcess().getId(), childSubmission);
                        }
                        // Pending to know if there is a task In Progress prior a Success task
                    }

                    while (itWarning.hasNext()) {
                        if (submission.getStatus().getName().equalsIgnoreCase("failed")) {
                            failed_counter++;
                        }
                        if (submission.getStatus().getName().equalsIgnoreCase("warning")) {
                            warning_counter++;
                        }
                        ChildSubmission childSubmissionWarning = itWarning.next();
                        if (childSubmissionWarning.getStatus().getName().equalsIgnoreCase("failed")
                                && (!successProcessMap.containsKey(childSubmissionWarning.getProcess().getId()))) {
                            failed_counter++;
                        }
                        if (childSubmissionWarning.getStatus().getName().equalsIgnoreCase("warning")
                                && (!successProcessMap.containsKey(childSubmissionWarning.getProcess().getId()))) {
                            warning_counter++;
                        }

                        Process predecessor = childSubmissionWarning.getProcess().getPredecessor();
                        if ((predecessor != null) && !submittedProcessMap.containsKey(predecessor.getId())) {
                            predessesor_not_submitted++;
                        }

                    }

                    parentSubmissionStatus = "in progress";

                    if (childrenProcesses.size() == successProcessMap.size()) {
                        parentSubmissionStatus = "success";
                        // setup end time with latest submission end time
                        parentSubmission.setEndTime(submission.getEndTime());
                    } else if (childrenProcesses.size() == successProcessMap.size()
                            && (submission.getStatus().getName().equalsIgnoreCase("in progress"))) {
                        parentSubmissionStatus = "success";
                    } else if (childrenProcesses.size() == submitted_child_counter && failed_counter > 0) {
                        parentSubmissionStatus = "failed";
                        // setup end time with latest submission end time
                        parentSubmission.setEndTime(submission.getEndTime());
                    } else if (childrenProcesses.size() == submitted_child_counter && (warning_counter > 0
                            || parentTimeBlockStatus(childrenProcesses).equalsIgnoreCase("delayed"))) {
                        parentSubmissionStatus = "warning";
                        // setup end time with latest submission end time
                        parentSubmission.setEndTime(submission.getEndTime());
                    } else if (successProcessMap.size() < submitted_child_counter
                            && (submission.getStatus().getName().equalsIgnoreCase("failed")) || failed_counter > 0) {
                        parentSubmissionStatus = "failed";
                    } else if (successProcessMap.size() < submitted_child_counter && (failed_counter == 0)
                            && (warning_counter > 0) || predessesor_not_submitted > 0) {
                        parentSubmissionStatus = "warning";
                    } else if ((childrenProcesses.size() > submitted_child_counter && (failed_counter == 0)
                            && (warning_counter == 0) && (predessesor_not_submitted == 0)
                            && (submission.getStatus().getName().equalsIgnoreCase("success")))) {
                        if (parentTimeBlockStatus(childrenProcesses).equalsIgnoreCase("delayed")) {
                            parentSubmissionStatus = "warning";
                        } else {
                            parentSubmissionStatus = "in progress";
                        }

                    }
                }
            }
            Status newStatus = getStatusByName(parentSubmissionStatus).get();
            parentSubmission.setStatus(newStatus);
            getNotificationForProcessAndStatus(parentSubmission.getProcess().getId(), newStatus.getId())
                    .ifPresent(not -> sendEmail(not, null, parentSubmission, parentSubmission.getProcess(), null));
            getSMSNotificationForProcessAndStatus(parentSubmission.getProcess().getId(), newStatus.getId())
                    .ifPresent(not -> sendSMS(not, null, parentSubmission, parentSubmission.getProcess(), null));
            parentSubmissionRepository.save(parentSubmission);
        }

    }

    public String parentTimeBlockStatus(List<Process> childrenProcesses) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH) - 1); // starting yesterday
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        Date start = today.getTime();
        Date now = new Date(); // to right now
        List<Long> processList = new ArrayList<Long>();
        String status = "on time";

        Iterator<Process> it = childrenProcesses.iterator();

        while (it.hasNext()) {
            Process process = it.next();
            processList.add(process.getId());
        }

        List<ScheduledSubmission> scheduledSubmissions = scheduledSubmissionRepository
                .findByStartTimeIsBetweenAndProcessIdIsInOrderByStartTimeAsc(start, now, processList);

        for (ScheduledSubmission scheduledSubmission : scheduledSubmissions) {
            status = "early";
            if (scheduledSubmission.getSubmission() != null) { // submission has not yet started
                if (now.getTime() > (scheduledSubmission.getEndTime().getTime()
                        + (scheduledSubmission.getTolerance() * MINUTES_1))) {
                    return "delayed";
                }
                if (now.getTime() > scheduledSubmission.getStartTime().getTime()) {
                    status = "on time";
                }
            }
        }

        return status;
    }

    private void updateSubmissionstatus(SubmissionStepRequest submissionStepRequest, Submission submission) {
        // If latest step is end & latest step before that one was Failed then status
        // submission is Failed
        // If latest step is end & latest step before that one was Warning then status
        // submission is Warning
        // If latest step is end & latest step before that one was Success and some
        // other step was either Failed or Warning then status Submission is Warning
        // If latest step is end & latest step before that one was Success and none step
        // was Warning or Failed then status Submission is Success
        submissionStepRequest.getProcessStepName();
        Set<SubmissionStep> submissionSteps = null;
        String submissionStatus = null;

        if (submissionStepRequest.getStatus() == null) {
            submissionStepRequest.setStatus("success");
        }

        if (submissionStepRequest.getProcessStepName().equalsIgnoreCase("end")) {
            submissionStatus = "success";

            switch (submissionStepRequest.getStatus().toLowerCase()) {
            case "success":
                submissionSteps = submission.getSteps();
                if (!submissionSteps.isEmpty()) {
                    Iterator<SubmissionStep> it = submissionSteps.iterator();
                    // Success

                    while (it.hasNext()) {
                        SubmissionStep subStep = it.next();
                        if (!subStep.getProcessStep().getName().equalsIgnoreCase("end")
                                && (subStep.getStatus().getName().equalsIgnoreCase("warning")
                                        || subStep.getStatus().getName().equalsIgnoreCase("failed"))) {
                            // Warning
                            submissionStatus = "warning";
                        }
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
        Status newStatus = getStatusByName(submissionStatus).get();
        submission.setStatus(newStatus);
        getNotificationForProcessAndStatus(submission.getProcess().getId(), newStatus.getId())
                .ifPresent(not -> sendEmail(not, null, null, submission.getProcess(), submission));
        getSMSNotificationForProcessAndStatus(submission.getProcess().getId(), newStatus.getId())
                .ifPresent(not -> sendSMS(not, null, null, submission.getProcess(), submission));
        submissionRepository.save(submission);
    }

    @PostMapping("/")    
    SubmissionStep create(Principal principal, @Valid @RequestBody SubmissionStepRequest submissionStepRequest) {        
        Long processId = Long.parseLong(principal.getName());
        Process process = processRepository.findById(processId).orElseThrow(NotFoundException::new);
        if (process.getApproved() != 'A') {
            throw new RuntimeException("Process should be approved");
        }
        this.systemTime = new Date();
        ProcessStep processStep = getProcessStepByProcessIdAndName(processId,
                submissionStepRequest.getProcessStepName())
                        .orElseThrow(() -> new IllegalArgumentException(
                                String.format("Step '%s' is not available. Please double check your process setup.",
                                        submissionStepRequest.getProcessStepName())));
        Optional<Status> stepStatus = Optional.empty();
        if (submissionStepRequest.getStatus() != null) {
            Status status = getStatusByName(submissionStepRequest.getStatus())
                    .orElseThrow(() -> new IllegalArgumentException(
                            String.format("Status '%s' is not supported. Please try with success, warning or failed",
                                    submissionStepRequest.getStatus())));
            stepStatus = Optional.of(status);
        }

        if (submissionStepRequest.getProcessStepName().equalsIgnoreCase(ProcessStep.START)) {
            return openNewSubmission(processId, processStep, submissionStepRequest);
        } else {
            Submission submission = getSubmission(submissionStepRequest, process);
            return updateSubmissionAndStep(processStep, submission, submissionStepRequest, stepStatus);
        }
    }

    private Submission getSubmission(SubmissionStepRequest submissionStepRequest, Process process) {
        Submission submission = null;
        if (submissionStepRequest.getSubmissionId() != null) {
            submission = submissionRepository.findById(submissionStepRequest.getSubmissionId())
                    .orElseThrow(NotFoundException::new);
        } else if (submissionStepRequest.getAltSubmissionId() != null) {
            submission = submissionRepository
                    .findFirstByProcessIdAndAltIdIgnoreCase(process.getId(), submissionStepRequest.getAltSubmissionId())
                    .orElseThrow(NotFoundException::new);
        } else {
            submission = getLatestOpenSubmissionByProcessId(process.getId()).orElseThrow(NotFoundException::new);
        }
        return submission;
    }

    private void updateSubmissionPeriod(Submission submission, String period) {
        if (period != null && !period.isEmpty()) {
            submission.setPeriod(period);
            char firstChar = period.charAt(0);
            switch (firstChar) {
            case 'Y':
                submission.setPeriodYear(Integer.valueOf(period.substring(1, 5)));
                submission.setPeriodMonth(0);
                submission.setPeriodQuarter(0);
                break;
            case 'M':
                int month = Integer.valueOf(period.substring(4, period.length()));
                submission.setPeriodYear(Integer.valueOf("20" + period.substring(1, 3)));
                submission.setPeriodMonth(month);
                if ((month > 0) && (month < 4)) {
                    submission.setPeriodQuarter(1);
                } else if ((month > 3) && (month < 7)) {
                    submission.setPeriodQuarter(2);
                } else if ((month > 6) && (month < 10)) {
                    submission.setPeriodQuarter(3);
                } else if ((month > 9) && (month < 13)) {
                    submission.setPeriodQuarter(4);
                }

                // submission.setPeriodQuarter(Integer.valueOf(period.substring(1, 4)));
                break;
            case 'Q':
                submission.setPeriodYear(Integer.valueOf("20" + period.substring(1, 3)));
                submission.setPeriodQuarter(Integer.valueOf(period.substring(5, 6)));
                submission.setPeriodMonth(0);
                break;
            default:
                submission.setPeriodYear(0);
                submission.setPeriodMonth(0);
                submission.setPeriodQuarter(0);
                break;
            }
        }

    }

    private void autoAcknowledgeSubmission(Submission submission) {
        // find latest submission
        // is failed?
        // got failed within one hour or earlier

        // autoacnowlege the failed submission,
        // comment
        Date now = new Date();

        Optional<Submission> optSubmission = submissionRepository.findFirstByProcessIdAndIdIsNotOrderByStartTimeDesc(
                submission.getProcess().getId(), submission.getId());

        if (optSubmission.isPresent()) {
            Submission latestSubmission = optSubmission.get();

            if (latestSubmission != null) {
                long diff = now.getTime() - latestSubmission.getStartTime().getTime();

                if (latestSubmission.getStatus().getName().equalsIgnoreCase("failed") && (diff < MINUTES_1 * 60)) {
                    if (submission.getAltId() == null || (submission.getAltId() != null
                            && submission.getAltId().equalsIgnoreCase(latestSubmission.getAltId()))) {
                        latestSubmission.setAcknowledgementFlag(true);
                        latestSubmission.setAcknowledgementDate(new Date());
                        latestSubmission
                                .setAcknowledgementNote("Auto Aknowledged by new submission ID: " + submission.getId());

                    }
                }
            }
        }

    }

    // Closes any pending Step that could stay in progress before ending the
    // Submission
    private void closeParallelPendingSteps(Submission submission) {
        List<SubmissionStep> pendingSubmissionSteps = submissionStepRepository
                .findBySubmissionIdAndEndTimeIsNullOrderByStartTimeDesc(submission.getId());

        Iterator<SubmissionStep> it = pendingSubmissionSteps.iterator();

        while (it.hasNext()) {
            SubmissionStep submissionStep = it.next();

            submissionStep.setEndTime(this.systemTime);
            submissionStep.setStatus(getStatusByName("success").get());
            submissionStepRepository.save(submissionStep);
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
}
