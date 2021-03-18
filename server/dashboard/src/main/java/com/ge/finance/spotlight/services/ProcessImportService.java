package com.ge.finance.spotlight.services;

import com.ge.finance.spotlight.exceptions.ConflictException;
import com.ge.finance.spotlight.exceptions.NotFoundException;
import com.ge.finance.spotlight.models.*;
import com.ge.finance.spotlight.models.Process;
import com.ge.finance.spotlight.models.System;
import com.ge.finance.spotlight.repositories.*;
import com.ge.finance.spotlight.responses.ProcessExport;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProcessImportService {

    private ClosePhaseRepository closePhaseRepository;
    private FeedTypeRepository feedTypeRepository;
    private MessageGatewayRepository messageGatewayRepository;
    private NotificationRepository notificationRepository;
    private ProcessRepository processRepository;
    private ProcessStepRepository processStepRepository;
    private ProcessTypeRepository processTypeRepository;
    private ProcessUserRepository processUserRepository;
    private RoleRepository roleRepository;
    private ScheduleDefinitionRepository scheduleDefinitionRepository;
    private ScheduledSubmissionService scheduledSubmissionService;
    private StatusRepository statusRepository;
    private SystemRepository systemRepository;
    private UserRepository userRepository;
    private NotificationMobileRepository notificationMobileRepository;
    private ProcessParentChildRepository processParentChildRepository;

    public ProcessImportService(ClosePhaseRepository closePhaseRepository, FeedTypeRepository feedTypeRepository,
            MessageGatewayRepository messageGatewayRepository, NotificationRepository notificationRepository,
            ProcessRepository processRepository, ProcessStepRepository processStepRepository,
            ProcessTypeRepository processTypeRepository, ProcessUserRepository processUserRepository,
            RoleRepository roleRepository, ScheduleDefinitionRepository scheduleDefinitionRepository,
            ScheduledSubmissionService scheduledSubmissionService, StatusRepository statusRepository,
            SystemRepository systemRepository, UserRepository userRepository,
            ProcessParentChildRepository processParentChildRepository,
            NotificationMobileRepository notificationMobileRepository) {
        this.closePhaseRepository = closePhaseRepository;
        this.feedTypeRepository = feedTypeRepository;
        this.messageGatewayRepository = messageGatewayRepository;
        this.notificationRepository = notificationRepository;
        this.processRepository = processRepository;
        this.processStepRepository = processStepRepository;
        this.processTypeRepository = processTypeRepository;
        this.processUserRepository = processUserRepository;
        this.roleRepository = roleRepository;
        this.scheduleDefinitionRepository = scheduleDefinitionRepository;
        this.scheduledSubmissionService = scheduledSubmissionService;
        this.statusRepository = statusRepository;
        this.systemRepository = systemRepository;
        this.userRepository = userRepository;
        this.notificationMobileRepository = notificationMobileRepository;
        this.processParentChildRepository = processParentChildRepository;
    }

    String simpleRandomStringGenerator() {
        int leftLimit = 48;
        int rightLimit = 122;
        int length = 6;
        return new Random().ints(leftLimit, rightLimit + 1).filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(length).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    ClosePhase getOrCreateClosePhaseByName(String closePhaseName) {
        return closePhaseRepository.findFirstByName(closePhaseName).orElseGet(() -> {
            ClosePhase closePhase = new ClosePhase();
            closePhase.setName(closePhaseName);
            return closePhaseRepository.save(closePhase);
        });
    }

    FeedType getOrCreateFeedTypeByName(String feedTypeName) {
        return feedTypeRepository.findFirstByName(feedTypeName).orElseGet(() -> {
            FeedType feedType = new FeedType();
            feedType.setName(feedTypeName);
            return feedTypeRepository.save(feedType);
        });
    }

    MessageGateway getOrCreateMessageGatewayByCarrierAndCountry(MessageGateway messageGateway) {
        String carrier = messageGateway.getCarrier().split("~")[0];
        return messageGatewayRepository.findFirstByCarrierAndCountry(carrier, messageGateway.getCountry())
                .orElseGet(() -> {
                    MessageGateway newMessageGateway = new MessageGateway();
                    newMessageGateway.setCarrier(carrier);
                    newMessageGateway.setCountry(messageGateway.getCountry());
                    newMessageGateway.setGateway(messageGateway.getGateway());
                    return messageGatewayRepository.save(newMessageGateway);
                });
    }

    Process getOrCreateProcessByName(Process process) {
        return processRepository.findFirstByName(process.getName())
                .orElseGet(() -> createProcess(process, Optional.empty()));
    }

    ProcessType getOrCreateProcessTypeByName(String processTypeName) {
        return processTypeRepository.findFirstByName(processTypeName).orElseGet(() -> {
            ProcessType processType = new ProcessType();
            processType.setName(processTypeName);
            return processTypeRepository.save(processType);
        });
    }

    System getOrCreateSystemByName(System system) {
        return systemRepository.findFirstByName(system.getName()).orElseGet(() -> {
            System newSystem = new System();
            newSystem.setName(system.getName());
            newSystem.setAppOwner(system.getAppOwner() != null ? getOrCreateUserBySSO(system.getAppOwner()) : null);
            newSystem.setClosePhase(
                    system.getClosePhase() != null ? getOrCreateClosePhaseByName(system.getClosePhase().getName())
                            : null);
            return systemRepository.save(newSystem);
        });
    }

    User getOrCreateUserBySSO(User user) {
        return userRepository.findOptionalBySSO(user.getSso()).orElseGet(() -> {
            User newUser = new User();
            newUser.setName(user.getName());
            newUser.setSso(user.getSso());
            newUser.setRole(
                    user.getRole() != null
                            ? roleRepository.findFirstByDescription(user.getRole().getDescription())
                                    .orElseThrow(NotFoundException::new)
                            : null);
            newUser.setCarrier(
                    user.getCarrier() != null ? getOrCreateMessageGatewayByCarrierAndCountry(user.getCarrier()) : null);
            newUser.setPhoneNumber(user.getPhoneNumber());
            return userRepository.save(user);
        });
    }

    Notification createNotification(Notification notification, Long processId) {
        Notification newNotification = new Notification();
        newNotification.setProcessId(processId);
        newNotification.setProcessStep(notification.getProcessStep() != null
                ? processStepRepository.findFirstByNameAndProcessId(notification.getProcessStep().getName(), processId)
                        .orElseThrow(NotFoundException::new)
                : null);
        newNotification.setStatus(notification.getStatus() != null ? statusRepository
                .findFirstByName(notification.getStatus().getName()).orElseThrow(NotFoundException::new) : null);
        newNotification.setAdditionalEmails(notification.getAdditionalEmails());
        newNotification.setEnableTextMessaging(notification.getEnableTextMessaging());
        newNotification.setCreatedBy(
                notification.getCreatedBy() != null ? getOrCreateUserBySSO(notification.getCreatedBy()) : null);
        newNotification.setEscalationType(notification.getEscalationType());
        newNotification.setSubmissionType(notification.getSubmissionType());
        newNotification = notificationRepository.save(newNotification);
        // Add Mobile records
        Iterator<NotificationMobile> iNotificationMobile = notification.getUserMobiles().iterator();
        while (iNotificationMobile.hasNext()) {
            NotificationMobile tempNotificationMobile = iNotificationMobile.next();
            NotificationMobile newNotificationMobile = new NotificationMobile();
            newNotificationMobile.setNotificationId(newNotification.getId());
            newNotificationMobile.setUser(
                    tempNotificationMobile.getUser() != null ? getOrCreateUserBySSO(tempNotificationMobile.getUser())
                            : null);
            notificationMobileRepository.save(newNotificationMobile);
        }
        return newNotification;
    }

    ProcessParentChild createProcessParentChild(ProcessParentChild processChild, Long processId) {

        Process process = processRepository.findFirstByName(processChild.getProcess().getName())
                .orElseThrow(NotFoundException::new);

        ProcessList parentProcess = new ProcessList();
        parentProcess.setName(process.getName());
        parentProcess.setReceiver(process.getReceiver());
        parentProcess.setSender(process.getSender());
        parentProcess.setId(process.getId());

        Process cProcess = processRepository.findById(processId).orElseThrow(NotFoundException::new);
        ProcessList childProcess = new ProcessList();
        childProcess.setName(cProcess.getName());
        childProcess.setReceiver(cProcess.getReceiver());
        childProcess.setSender(cProcess.getSender());
        childProcess.setId(cProcess.getId());

        ProcessParentChild processParentChild = new ProcessParentChild();
        processParentChild.setProcess(parentProcess);
        processParentChild.setProcessChild(childProcess);
        processParentChild.setSeq(processChild.getSeq());
        return processParentChildRepository.save(processParentChild);
    }

    Process createProcess(Process process, Optional<String> newName) {

        Process newProcess = new Process();
        newProcess.setName(newName.orElse(process.getName()));
        newProcess.setSender(process.getSender() != null ? getOrCreateSystemByName(process.getSender()) : null);
        newProcess.setReceiver(process.getReceiver() != null ? getOrCreateSystemByName(process.getReceiver()) : null);
        newProcess.setProcessType(
                process.getProcessType() != null ? getOrCreateProcessTypeByName(process.getProcessType().getName())
                        : null);
        newProcess.setFeedType(
                process.getFeedType() != null ? getOrCreateFeedTypeByName(process.getFeedType().getName()) : null);
        newProcess.setCritical(process.getCritical());
        newProcess.setProcessParent(
                process.getProcessParent() != null ? getOrCreateProcessByName(process.getProcessParent()) : null);
        newProcess.setProcessLevel(process.getProcessLevel());
        newProcess.setIsParent(process.getIsParent());
        newProcess.setTechnicalOwner(
                process.getTechnicalOwner() != null ? getOrCreateUserBySSO(process.getTechnicalOwner()) : null);
        newProcess.setFunctionalOwner(
                process.getFunctionalOwner() != null ? getOrCreateUserBySSO(process.getFunctionalOwner()) : null);
        newProcess.setAppOwner(process.getAppOwner() != null ? getOrCreateUserBySSO(process.getAppOwner()) : null);
        newProcess.setApproved(process.getApproved());
        newProcess.setSupportTeamEmail(process.getSupportTeamEmail());
        newProcess.setMaxRunTimeHours(process.getMaxRunTimeHours());
        newProcess.setMaxRunTimeMinutes(process.getMaxRunTimeMinutes());
        newProcess.setSubmissionEscalationAlrt(process.getSubmissionEscalationAlrt());
        newProcess.setSubmissionDelayedEscalationAlrt(process.getSubmissionDelayedEscalationAlrt());
        newProcess.setLongRunningSubAlrt(process.getLongRunningSubAlrt());
        newProcess.setLongRunningStepAlrt(process.getLongRunningStepAlrt());
        newProcess.setRequiredStepAlrt(process.getRequiredStepAlrt());
        java.lang.System.out.println(process.getName());
        return processRepository.save(newProcess);

    }

    ProcessStep createProcessStep(ProcessStep processStep, Long processId) {
        ProcessStep newProcessStep = new ProcessStep();
        newProcessStep.setProcessId(processId);
        newProcessStep.setAssociatedStepId(processStep.getAssociatedStepId());
        newProcessStep.setName(processStep.getName());
        newProcessStep.setDuration((processStep.getDuration() == null ? 0 : processStep.getDuration()));
        newProcessStep.setManualDuration(processStep.getManualDuration());
        newProcessStep.setRequired(processStep.getRequired());
        newProcessStep.setParallel(processStep.getParallel());
        newProcessStep.setDisabled(processStep.getDisabled());
        return processStepRepository.save(newProcessStep);
    }

    ScheduleDefinition createScheduleDefinition(ScheduleDefinition scheduleDefinition, Process process) {
        ScheduleDefinition newScheduleDefinition = new ScheduleDefinition();
        newScheduleDefinition.setProcess(process);
        newScheduleDefinition.setProcessWorkflowId(scheduleDefinition.getProcessWorkflowId());
        newScheduleDefinition.setSettings(scheduleDefinition.getSettings());
        newScheduleDefinition.setTolerance(scheduleDefinition.getTolerance());
        newScheduleDefinition.setScheduleChangeFlag(scheduleDefinition.getScheduleChangeFlag());
        newScheduleDefinition.setStartTime(scheduleDefinition.getStartTime());
        newScheduleDefinition.setEndTime(scheduleDefinition.getEndTime());
        newScheduleDefinition.setScheduleStartDate(scheduleDefinition.getScheduleStartDate());
        newScheduleDefinition.setScheduleEndDate(scheduleDefinition.getScheduleEndDate());
        newScheduleDefinition.setRecurrencePattern(scheduleDefinition.getRecurrencePattern());
        newScheduleDefinition.setRecurrenceTime(scheduleDefinition.getRecurrenceTime());
        return scheduleDefinitionRepository.save(newScheduleDefinition);
    }

    public Process importProcessWithName(Process process, String name) {
        Optional<String> newName = Optional.of(name);
        if (processRepository.existsByNameIgnoreCase(name)) {
            newName = Optional.of(String.format("%s (Copy %s)", name, simpleRandomStringGenerator()));
        }
        return createProcess(process, newName);
    }

    public Process importProcess(Process process) {
        Optional<String> newName = Optional.empty();
        if (!process.getIsParent() && process.getProcessParent() != null) {
            if (processRepository.existsByNameIgnoreCase(process.getProcessParent().getName())) {
                if (processRepository.existsByNameIgnoreCase(process.getName())) {
                    newName = Optional
                            .of(String.format("%s (Copy %s)", process.getName(), simpleRandomStringGenerator()));
                }
            } else {
                throw new ConflictException(
                        "Task failed successfully : Parent Process Missing.Please Import Parent Process.");
            }
        } else if (process.getProcessParent() == null) {
            if (processRepository.existsByNameIgnoreCase(process.getName())) {
                newName = Optional.of(String.format("%s (Copy %s)", process.getName(), simpleRandomStringGenerator()));
            }
        }
        return createProcess(process, newName);
    }

    public Collection<ProcessStep> importProcessSteps(Collection<ProcessStep> processSteps, Long processId) {
        Map<Long, ProcessStep> imported = new HashMap<>();
        Collection<String> existingStepNames = processStepRepository.findByProcessId(processId).stream()
                .map(ProcessStep::getName).collect(Collectors.toList());
        for (ProcessStep processStep : processSteps) {
            if (!existingStepNames.contains(processStep.getName())) {
                imported.put(processStep.getId(), createProcessStep(processStep, processId));
            }
        }
        for (ProcessStep newProcessStep : imported.values()) {
            if (newProcessStep.getAssociatedStepId() != null) {
                newProcessStep.setAssociatedStepId(imported.get(newProcessStep.getAssociatedStepId()).getId());
            }
        }
        return processStepRepository.saveAll(imported.values());
    }

    /*
     * public Collection<ProcessParentChild>
     * importProcessParentChild(Collection<ProcessParentChild> processParentChilds,
     * Long processId) { return processParentChilds.stream().map(processChild ->
     * createProcessParentChild(processChild, processId))
     * .collect(Collectors.toList()); }
     */

    public ProcessParentChild importProcessParentChild(ProcessParentChild processParentChilds, Long processId) {
        return createProcessParentChild(processParentChilds, processId);
    }

    public Collection<Notification> importNotifications(Collection<Notification> notifications, Long processId) {
        return notifications.stream().map(notification -> createNotification(notification, processId))
                .collect(Collectors.toList());
    }

    public Collection<ProcessUser> importUsers(Collection<User> users, Long processId) {
        java.lang.System.out.println(users.size());
        Collection<ProcessUser> newProcessUsers = users.stream().map(user -> {
            ProcessUser processUser = new ProcessUser();
            processUser.setUser(getOrCreateUserBySSO(user));
            processUser.setProcessId(processId);
            return processUser;
        }).collect(Collectors.toList());
        return processUserRepository.saveAll(newProcessUsers);
    }

    public Collection<ScheduleDefinition> importScheduleDefinitions(Collection<ScheduleDefinition> scheduleDefinitions,
            Long processId) {
        Process process = processRepository.findById(processId).orElseThrow(NotFoundException::new);
        return scheduleDefinitions.stream().map(scheduleDefinition -> {
            ScheduleDefinition newScheduleDefinition = createScheduleDefinition(scheduleDefinition, process);
            scheduledSubmissionService.setupScheduledSubmissions(newScheduleDefinition, null);
            return newScheduleDefinition;
        }).collect(Collectors.toList());
    }

}
