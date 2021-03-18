package com.ge.finance.spotlight.controllers;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.validation.Valid;

import com.auth0.jwt.JWT;
import com.ge.finance.spotlight.dto.EmailModel;
import com.ge.finance.spotlight.dto.ProcessAlert;
import com.ge.finance.spotlight.dto.ProcessApprovalDTO;
import com.ge.finance.spotlight.dto.ProcessDTO;
import com.ge.finance.spotlight.dto.ScheduleDefinitionDTO;
import com.ge.finance.spotlight.exceptions.ConflictException;
import com.ge.finance.spotlight.exceptions.ForbiddenException;
import com.ge.finance.spotlight.exceptions.NotFoundException;
import com.ge.finance.spotlight.models.*;
import com.ge.finance.spotlight.models.Process;
import com.ge.finance.spotlight.models.System;
import com.ge.finance.spotlight.repositories.*;
import com.ge.finance.spotlight.requests.*;
import com.ge.finance.spotlight.responses.ProcessExport;
import com.ge.finance.spotlight.services.ProcessImportService;
import com.ge.finance.spotlight.services.ScheduledSubmissionService;
import com.ge.finance.spotlight.services.SpotlightEmailService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/processes")
public class ProcessController {

    @Value("${app_secret}")
    private String app_secret;

    private static final long ADMIN = 1;
    private static final long USER = 2;
    private static final long APPLICATION = 3;

    private ProcessRepository processRepository;
    private ProcessStepRepository processStepRepository;
    private ProcessUserRepository processUserRepository;
    private UserRepository userRepository;
    private NotificationRepository notificationRepository;
    private StatusRepository statusRepository;
    private ScheduleDefinitionRepository scheduleDefinitionRepository;
    private ScheduledSubmissionService scheduledSubmissionService;
    private ScheduledSubmissionRepository scheduledSubmissionRepository;
    private SubmissionRepository submissionRepository;
    private SpotlightEmailService spotlightEmailService;
    private RoleRepository roleRepository;
    private ProcessParentChildRepository processParentChildRepository;
    private SubmissionStepRepository submissionStepRepository;
    private ProcessExportRequestRepository processExportRequestRepository;
    private ProcessImportService processImportService;
    private ScheduleCriticalDefinitionRepository scheduleCriticalDefinitionRepository;
    private NotificationMobileRepository notificationMobileRepository;
    private UserPermissionRepository userPermissionRepository;
    private NotificationLogRepository notificationLogRepository;

    static class Daily {
        public int recurEvery;
        public int dayRecurrence;
        public String option;
        public int daysNumber;
        public int monthsNumber;
        public int ocurrence;
        public String day;
        public int every;
        public boolean isEveryWeekday;
        public int recurEveryHour;
        public int timeRecurrence;
        public String hourlyStartTime;
        public String hourlyEndTime;
        public int hourlyDuration;
    }

    public ProcessController(ProcessRepository processRepository, ProcessStepRepository processStepRepository,
            UserRepository userRepository, ProcessUserRepository processUserRepository,
            NotificationRepository notificationRepository, StatusRepository statusRepository,
            ScheduleDefinitionRepository scheduleDefinitionRepository,
            ScheduledSubmissionService scheduledSubmissionService,
            ScheduledSubmissionRepository scheduledSubmissionRepository, SpotlightEmailService spotlightEmailService,
            RoleRepository roleRepository, ProcessParentChildRepository processParentChildRepository,
            SubmissionStepRepository submissionStepRepository,
            ProcessExportRequestRepository processExportRequestRepository, ProcessImportService processImportService,
            ScheduleCriticalDefinitionRepository scheduleCriticalDefinitionRepository,
            NotificationMobileRepository notificationMobileRepository,
            UserPermissionRepository userPermissionRepository, NotificationLogRepository notificationLogRepository,
            SubmissionRepository submissionRepository) {
        this.processRepository = processRepository;
        this.processStepRepository = processStepRepository;
        this.userRepository = userRepository;
        this.processUserRepository = processUserRepository;
        this.notificationRepository = notificationRepository;
        this.statusRepository = statusRepository;
        this.scheduleDefinitionRepository = scheduleDefinitionRepository;
        this.scheduleCriticalDefinitionRepository = scheduleCriticalDefinitionRepository;
        this.scheduledSubmissionService = scheduledSubmissionService;
        this.scheduledSubmissionRepository = scheduledSubmissionRepository;
        this.spotlightEmailService = spotlightEmailService;
        this.roleRepository = roleRepository;
        this.processParentChildRepository = processParentChildRepository;
        this.submissionStepRepository = submissionStepRepository;
        this.processExportRequestRepository = processExportRequestRepository;
        this.processImportService = processImportService;
        this.notificationMobileRepository = notificationMobileRepository;
        this.userPermissionRepository = userPermissionRepository;
        this.notificationLogRepository = notificationLogRepository;
        this.submissionRepository = submissionRepository;
    }

    private boolean isAdmin(Collection<? extends GrantedAuthority> grantedAuthorities) {
        for (GrantedAuthority grantedAuthority : grantedAuthorities) {
            if (grantedAuthority.getAuthority().equals("admin")) {
                return true;
            }
        }
        return false;
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

    private boolean isApplication(Collection<? extends GrantedAuthority> grantedAuthorities) {
        for (GrantedAuthority grantedAuthority : grantedAuthorities) {
            if (grantedAuthority.getAuthority().equals("application")) {
                return true;
            }
        }
        return false;
    }

    private boolean isProcessUser(Long sso, Long processId) {
        User user = userRepository.findFirstBySso(sso);
        List<ProcessUser> processUsers = processUserRepository.findByUserIdAndProcessId(user.getId(), processId);
        return !processUsers.isEmpty();
    }

    private Optional<ProcessUser> getProcessUserByUserIdAndProcessId(Long userId, Long processId) {
        List<ProcessUser> processUsers = processUserRepository.findByUserIdAndProcessId(userId, processId);
        if (processUsers.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(processUsers.get(0));
        }
    }

    private boolean existsByScheduleDefinitionIdAndProcessId(Long scheduleDefinitionId, Long processId) {
        return !scheduleDefinitionRepository.findByIdAndProcessId(scheduleDefinitionId, processId).isEmpty();
    }

    private boolean existsByScheduleCriticalDefinitionIdAndProcessId(Long scheduleDefinitionId, Long processId) {
        return !scheduleCriticalDefinitionRepository.findByIdAndProcessId(scheduleDefinitionId, processId).isEmpty();
    }

    @GetMapping("/")
    List<Process> index(Authentication authentication) {
        if (isAdmin(authentication.getAuthorities())) {
            return processRepository.findAll();
        } else {
            Long sso = (Long) authentication.getPrincipal();
            User user = userRepository.findFirstBySso(sso);
            List<Long> processIdList = processUserRepository.findByUserId(user.getId()).stream()
                    .map(ProcessUser::getProcessId).collect(Collectors.toList());
            if (processIdList.isEmpty())
                return Collections.emptyList();
            return processRepository.findByIdIsIn(processIdList);
        }
    }

    @GetMapping("/assignable")
    List<ProcessDTO> assignableProcesses(Authentication authentication) {
        return processRepository.findByIsParentAndProcessParentIdOrderByName('N', null);
    }

    @GetMapping("/all")
    List<Process> indexAll() {
        return processRepository.findAll();
    }

    @GetMapping("/submit-permission")
    Collection<Process> bySubmitPermission(Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        Collection<UserPermission> userPermissions = userPermissionRepository.findBySSOAndPermission(sso, "submit");
        Collection<Long> senderIdList = userPermissions.stream().map(UserPermission::getSender).map(System::getId)
                .collect(Collectors.toList());
        Collection<Long> receiverIdList = userPermissions.stream().map(UserPermission::getReceiver).map(System::getId)
                .collect(Collectors.toList());
        return processRepository.findBySenderIdInAndReceiverIdIn(senderIdList, receiverIdList);
    }

    @PostMapping("/")
    Process create(@RequestBody Process process, Authentication authentication) {

        process.setName(process.getName().trim());

        if (processRepository.existsByNameIgnoreCase(process.getName())) {
            throw new RuntimeException(
                    "Process Name Already Exists.  Please choose a unique process name or request access to the existing process.");
        }

        if (isAdmin(authentication.getAuthorities())) {
            process.setApproved('A');
            process = processRepository.save(process);
        } else {
            // Send Email
            Long sso = (Long) authentication.getPrincipal();
            User requestor = userRepository.findFirstBySso(sso);
            process = processRepository.save(process);
            newProcessUser(process.getId(), requestor.getId());
            EmailModel emailModel = new EmailModel(NotificationTemplate.PROCESS_APPROVAL,
                    "jamie.myers@ge.com SpotlightDevTeam@ge.com", false);
            emailModel.process = process;
            emailModel.user = requestor;
            this.spotlightEmailService.genericSend(emailModel);
        }
        if (!process.getIsParent()) {
            createDefaultProcessSteps(process.getId());
        }
        return process;
    }

    private void createDefaultProcessSteps(Long processId) {
        ProcessStep start = new ProcessStep();
        start.setProcessId(processId);
        start.setName(ProcessStep.START);
        start.setDuration(0L);
        processStepRepository.save(start);
        ProcessStep end = new ProcessStep();
        end.setProcessId(processId);
        end.setDuration(0L);
        end.setName(ProcessStep.END);
        processStepRepository.save(end);
    }

    @GetMapping("/{processId}")
    Process get(@PathVariable(value = "processId") Long processId) {
        return processRepository.findById(processId).orElseThrow(NotFoundException::new);
    }

    @GetMapping("/list")
    List<ProcessDTO> getPro(Authentication authentication) {
        if (isAdmin(authentication.getAuthorities())) {
            return processRepository.findAllProcess();
        } else {
            Long sso = (Long) authentication.getPrincipal();
            User user = userRepository.findFirstBySso(sso);
            List<Long> processIdList = processUserRepository.findByUserId(user.getId()).stream()
                    .map(ProcessUser::getProcessId).collect(Collectors.toList());
            if (!processIdList.isEmpty()) {
                return processRepository.findByIdAllProcess(processIdList);
            } else {
                throw new RuntimeException("User is not added to any process");
            }
        }
    }

    @GetMapping("/{processId}/children")
    List<Process> getChildren(@PathVariable(value = "processId") Long processId) {
        return processRepository.findByProcessParentId(processId);
    }

    @PutMapping("/{processId}")
    Process update(@RequestBody Process process, @PathVariable(value = "processId") Long processId,
            Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        if (!isProcessUser(sso, processId)) {
            throw new ForbiddenException();
        }

        if (processRepository.existsById(processId)) {
            return processRepository.save(process);
        } else {
            throw new NotFoundException();
        }
    }

    @DeleteMapping("/{processId}")
    @PreAuthorize("hasAuthority('admin')")
    Process delete(@PathVariable(value = "processId") Long processId) {
        Process process = processRepository.findById(processId).orElseThrow(NotFoundException::new);
        if (process.getIsParent() && process.getProcessParent() == null
                && processParentChildRepository.existsByProcessId(processId)) {
            throw new ConflictException("Please delete the child process before deleteing the parent process");
        } else {
            try {
                if (scheduledSubmissionRepository.findByProcessId(processId).size() > 0) {
                    scheduledSubmissionRepository.deleteAll(scheduledSubmissionRepository.findByProcessId(processId));
                }
                if (scheduleDefinitionRepository.findByProcessId(processId).size() > 0) {
                    scheduleDefinitionRepository.deleteAll(scheduleDefinitionRepository.findByProcessId(processId));
                }
                if (submissionRepository.findByProcessId(processId).size() > 0) {
                    for (Submission allData : submissionRepository.findByProcessId(processId)) {
                        submissionStepRepository
                                .deleteAll(submissionStepRepository.findBySubmissionIdOrderByIdAsc(allData.getId()));
                    }
                    submissionRepository.deleteAll(submissionRepository.findByProcessId(processId));
                }
                if (processExportRequestRepository.existsByProcessId(processId).isPresent()) {
                    processExportRequestRepository.deleteAll(processExportRequestRepository.findByProcessId(processId));
                }
                if (notificationLogRepository.findByProcessId(processId).size() > 0) {
                    notificationLogRepository.deleteAll(notificationLogRepository.findByProcessId(processId));
                }
                if (notificationRepository.findByProcessId(processId).size() > 0) {
                    notificationRepository.deleteAll(notificationRepository.findByProcessId(processId));
                }
                if (processStepRepository.existsByProcessId(processId)) {
                    for (ProcessStep allData : processStepRepository.findByProcessId(processId)) {
                        if (allData.getAssociatedStepId() != null) {
                            allData.setAssociatedStepId(null);
                            processStepRepository.save(allData);
                        }
                    }
                    processStepRepository.deleteAll(processStepRepository.findByProcessId(processId));
                }
                if (processUserRepository.existsByProcessId(processId)) {
                    processUserRepository.deleteAll(processUserRepository.findByProcessId(processId));
                }
                if (processParentChildRepository.existsByProcessId(processId)) {
                    processParentChildRepository
                            .deleteAll(processParentChildRepository.findByProcessIdOrderBySeqAscIdAsc(processId));
                }
                if (processParentChildRepository.findByChildId(processId).size() > 0) {
                    processParentChildRepository.deleteAll(processParentChildRepository.findByChildId(processId));
                }

                processRepository.delete(process);
                return process;

            } catch (Exception e) {
                throw new ConflictException("Process has steps, users or parent assigned");
            }
        }

    }

    @GetMapping("/{processId}/steps")
    List<ProcessStep> indexSteps(@PathVariable(value = "processId") Long processId) {
        return processStepRepository.findByProcessIdAndAssociatedStepIdIsNull(processId);
    }

    @GetMapping("/{processId}/steps/all")
    List<ProcessStep> indexStepsAll(@PathVariable(value = "processId") Long processId) {
        return processStepRepository.findByProcessId(processId);
    }

    @PostMapping("/{processId}/approve")
    Process approveProcess(@PathVariable(value = "processId") Long processId,
            @RequestBody ProcessApprovalDTO processAprovalDTO, Authentication authentication) {
        if (isAdmin(authentication.getAuthorities())) {
            Process process = processRepository.findById(processId).orElseThrow(NotFoundException::new);
            if (processAprovalDTO.isCheck()) {
                process.setApproved('A');
            } else {
                process.setApproved('N');
            }
            process = processRepository.save(process);
            User requestor = processUserRepository.findByProcessId(processId).get(0).getUser();
            String requestorEmail = requestor.getSso() + "@ge.com";
            EmailModel emailModel = new EmailModel(NotificationTemplate.PROCESS_APPROVAL_RESULT,
                    ("jamie.myers@ge.com SpotlightDevTeam@ge.com" + " " + requestorEmail), false);
            emailModel.process = process;
            emailModel.user = requestor;
            spotlightEmailService.genericSend(emailModel);
            return process;
        } else {
            throw new ForbiddenException();
        }
    }

    @PostMapping("/{processId}/steps")
    ProcessStep createStep(@PathVariable(value = "processId") Long processId, @RequestBody ProcessStep processStep,
            Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        List<ProcessStep> pStep = processStepRepository.findByProcessId(processId);
        for (ProcessStep allData : pStep) {
            if (processStep.getName().trim().equalsIgnoreCase(allData.getName().trim())) {
                throw new RuntimeException(
                        "Process Step Name Already Exists. Please choose a unique process step name or enable the step already added to invoke new submission.");
            }
        }
        if (processRepository.existsById(processId)) {
            if (isProcessUser(sso, processId)) {
                processStep.setProcessId(processId);
                processStep.setDuration(processStep.getDuration() != null ? processStep.getDuration() : 0L);
                return processStepRepository.save(processStep);
            } else {
                throw new ForbiddenException();
            }
        } else {
            throw new NotFoundException();
        }
    }

    @GetMapping("/{processId}/steps/{processStepId}")
    ProcessStep getStep(@PathVariable(value = "processId") Long processId,
            @PathVariable(name = "processStepId") Long processStepId, Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        if (processStepRepository.existsByIdAndProcessId(processStepId, processId)) {
            if (isAdmin(authentication.getAuthorities())) {
                return processStepRepository.findById(processStepId).orElseThrow(NotFoundException::new);
            } else {
                if (isProcessUser(sso, processId)) {
                    return processStepRepository.findById(processStepId).orElseThrow(NotFoundException::new);
                } else {
                    throw new ForbiddenException();
                }
            }
        } else {
            throw new NotFoundException();
        }
    }

    @PutMapping("/{processId}/steps/{processStepId}")
    ProcessStep updateStep(@PathVariable(value = "processId") Long processId,
            @PathVariable(value = "processStepId") Long processStepId, @RequestBody ProcessStep processStep,
            Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        List<SubmissionStep> submissionSteps = submissionStepRepository.findByProcessStepId(processStepId);
        if (isProcessUser(sso, processId) && processStepRepository.existsByIdAndProcessId(processStepId, processId)) {
            ProcessStep pStep = processStepRepository.findById(processStepId).orElseThrow(NotFoundException::new);
            if (!processStep.getName().equalsIgnoreCase(pStep.getName())) {
                if (!processStep.getDuration().equals(pStep.getDuration())) {
                    processStep.setManualDuration(processStep.getDuration());
                    processStep.setDuration(pStep.getDuration());
                }
                processStep.setProcessId(processId);
                pStep.setDisabled(true);
                processStepRepository.save(processStep);
                pStep.setAssociatedStepId(processStep.getId());
                processStepRepository.save(pStep);
            } else {
                if (!processStep.getDuration().equals(pStep.getDuration())) {
                    processStep.setManualDuration(processStep.getDuration());
                    processStep.setDuration(pStep.getDuration());
                }
                processStep.setId(processStepId);
                processStep.setProcessId(processId);
                processStepRepository.save(processStep);
            }
            for (SubmissionStep subSteps : submissionSteps) {
                subSteps.setProcessStep(processStep);
                submissionStepRepository.save(subSteps);
            }
        } else {
            throw new ForbiddenException();
        }
        return processStep;
    }

    @DeleteMapping("/{processId}/steps/{processStepId}")
    ProcessStep deleteStep(@PathVariable(value = "processId") Long processId,
            @PathVariable(value = "processStepId") Long processStepId, Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        if (processRepository.existsById(processId) && processStepRepository.existsById(processStepId)) {
            if (isProcessUser(sso, processId)) {

                if (submissionStepRepository.existsByProcessStepId(processStepId)) {
                    throw new RuntimeException("Step has been used in a submission and cannot be deleted.");
                }

                ProcessStep processStep = processStepRepository.findById(processStepId)
                        .orElseThrow(NotFoundException::new);
                processStepRepository.deleteById(processStepId);
                return processStep;
            } else {
                throw new ForbiddenException();
            }
        } else {
            throw new NotFoundException();
        }
    }

    @PostMapping("/{processId}/token")
    Map<String, String> createProcessToken(@PathVariable(value = "processId") Long processId,
            Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        if (isProcessUser(sso, processId)) {
            String token = JWT.create().withSubject(Long.toString(processId)).sign(HMAC512(app_secret.getBytes()));
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            return response;
        } else {
            throw new ForbiddenException();
        }
    }

    @GetMapping("/{processId}/users")
    List<ProcessUser> getProcessUsers(@PathVariable(value = "processId") Long processId) {
        return processUserRepository.findByProcessId(processId);
    }

    @PostMapping("/{processId}/users")
    List<ProcessUser> addUserToProcess(@PathVariable(value = "processId") Long processId,
            @RequestBody List<Long> processUserRequest, Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        if (isAdmin(authentication.getAuthorities()) || isProcessUser(sso, processId)) {
            List<ProcessUser> processUsers = new ArrayList<>();
            for (Long allData : processUserRequest) {
                processUsers.add(newProcessUser(processId, allData));
            }
            return processUsers;
        } else {
            throw new ForbiddenException();
        }
    }

    ProcessUser newProcessUser(Long processId, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(NotFoundException::new);
        if (processRepository.existsById(processId)) {
            ProcessUser processUser = new ProcessUser();
            processUser.setProcessId(processId);
            processUser.setUser(user);

            if (user.getRole().getId().equals(USER)) {
                user.setRole(roleRepository.findById(APPLICATION).orElseThrow(NotFoundException::new));
                userRepository.save(user);
            }
            return processUserRepository.save(processUser);
        } else {
            throw new NotFoundException();
        }
    }

    @DeleteMapping("/{processId}/users/{userId}")
    ProcessUser removeUserFromProcess(@PathVariable(value = "processId") Long processId,
            @PathVariable(value = "userId") Long processUserId, Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        if (isAdmin(authentication.getAuthorities()) || isProcessUser(sso, processId)) {
            ProcessUser processUser = getProcessUserByUserIdAndProcessId(processUserId, processId)
                    .orElseThrow(NotFoundException::new);
            processUserRepository.delete(processUser);
            return processUser;
        } else {
            throw new ForbiddenException();
        }
    }

    @GetMapping("/{processId}/my-notifications")
    Collection<Notification> getMyNotifications(@PathVariable Long processId, Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        User user = userRepository.findOptionalBySSO(sso).orElseThrow(NotFoundException::new);
        return notificationRepository.findByProcessIdAndUserId(processId, user.getId());
    }

    @PostMapping("/{processId}/my-notifications")
    Notification createNewMyNotification(@PathVariable Long processId,
            @Valid @RequestBody NotificationRequest notificationRequest, Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        User user = userRepository.findOptionalBySSO(sso).orElseThrow(NotFoundException::new);
        ProcessStep processStep = notificationRequest.getProcessStepId() != null ? processStepRepository
                .findById(notificationRequest.getProcessStepId()).orElseThrow(NotFoundException::new) : null;
        Status status = notificationRequest.getStatusId() != null
                ? statusRepository.findById(notificationRequest.getStatusId()).orElseThrow(NotFoundException::new)
                : null;
        Notification notification = new Notification();
        notification.setProcessId(processId);
        notification.setProcessStep(processStep);
        notification.setStatus(status);
        notification.setEnableTextMessaging(notificationRequest.getEnableTextMessaging());
        notification.setEscalationType(notificationRequest.getEscalationType());
        notification.setSubmissionType(notificationRequest.getSubmissionType());
        notification = notificationRepository.save(notification);
        NotificationMobile notificationMobile = new NotificationMobile();
        notificationMobile.setNotificationId(notification.getId());
        notificationMobile.setUser(user);
        notificationMobileRepository.save(notificationMobile);
        return notification;
    }

    @PatchMapping("/{processId}/my-notifications/{notificationId}")
    Notification updateMyNotification(@PathVariable Long processId, @PathVariable Long notificationId,
            @RequestBody NotificationRequest notificationRequest, Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        User user = userRepository.findOptionalBySSO(sso).orElseThrow(NotFoundException::new);
        Notification notification = notificationRepository.findFirstByIdAndUserId(notificationId, user.getId())
                .orElseThrow(NotFoundException::new);
        ProcessStep processStep = notificationRequest.getProcessStepId() != null ? processStepRepository
                .findById(notificationRequest.getProcessStepId()).orElseThrow(NotFoundException::new) : null;
        Status status = notificationRequest.getStatusId() != null
                ? statusRepository.findById(notificationRequest.getStatusId()).orElseThrow(NotFoundException::new)
                : null;
        notification.setProcessStep(processStep);
        notification.setStatus(status);
        notification.setEnableTextMessaging(notificationRequest.getEnableTextMessaging());
        notification.setEscalationType(notificationRequest.getEscalationType());
        notification.setSubmissionType(notificationRequest.getSubmissionType());
        return notificationRepository.save(notification);
    }

    @DeleteMapping("/{processId}/my-notifications/{notificationId}")
    Notification deleteMyNotification(@PathVariable Long processId, @PathVariable Long notificationId,
            Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        User user = userRepository.findOptionalBySSO(sso).orElseThrow(NotFoundException::new);
        NotificationMobile notificationMobile = notificationMobileRepository
                .findByNotificationIdAndUserId(notificationId, user.getId());
        notificationMobileRepository.delete(notificationMobile);
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(NotFoundException::new);
        notificationRepository.delete(notification);
        return notification;
    }

    @GetMapping("/{processId}/notifications")
    List<Notification> getProcessNotifications(@PathVariable Long processId, Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        if (isAdmin(authentication.getAuthorities()) || isProcessUser(sso, processId)) {
            return notificationRepository.findByProcessId(processId);
        } else {
            throw new ForbiddenException();
        }
    }

    @GetMapping("/{processId}/children-map")
    List<ProcessParentChild> getChildrenMap(@PathVariable Long processId, Authentication authentication) {
        Process process = processRepository.findById(processId).orElseThrow(NotFoundException::new);

        if (!process.getIsParent()) {
            if (process.getProcessParent() != null) {
                processId = process.getProcessParent().getId();
            } else {
                throw new RuntimeException("This process doesn't belong to any parent.");
            }
        }

        return this.processParentChildRepository.findByProcessIdOrderBySeqAscIdAsc(processId);
    }

    @PostMapping("/{processId}/children-map")
    @PreAuthorize("hasAuthority('admin')")
    ProcessParentChild addChildren(@PathVariable Long processId, @RequestBody ProcessParentChild processParentChild,
            Authentication authentication) {
        Process process = processRepository.findById(processParentChild.getProcessChild().getId()).get();
        Process parent = processRepository.findById(processParentChild.getProcess().getId()).get();
        process.setProcessParent(parent);
        processRepository.save(process);
        return this.processParentChildRepository.save(processParentChild);
    }

    @DeleteMapping("/{processId}/children-map/{childId}")
    @PreAuthorize("hasAuthority('admin')")
    void removeChildren(@PathVariable Long processId, @PathVariable Long childId, Authentication authentication) {
        try {
            ProcessParentChild ppc = processParentChildRepository.findById(childId).get();
            Process process = processRepository.findById(ppc.getProcessChild().getId()).get();
            process.setProcessParent(null);
            processRepository.save(process);
            this.processParentChildRepository.deleteById(childId);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    @PostMapping("/{processId}/notifications")
    Notification addNotificationToProcess(@PathVariable Long processId, @RequestBody Notification notification,
            Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        User user = userRepository.findFirstBySso(sso);
        if (isProcessUser(sso, processId)) {
            notification.setProcessId(processId);
            if (notification.getProcessStep() != null && notification.getProcessStep().getId() != null
                    && processStepRepository.existsByIdAndProcessId(notification.getProcessStep().getId(), processId)) {
                notification.setProcessStep(processStepRepository.findById(notification.getProcessStep().getId())
                        .orElseThrow(NotFoundException::new));
            }
            if (notification.getStatus() != null && notification.getStatus().getId() != null) {
                notification.setStatus(statusRepository.findById(notification.getStatus().getId())
                        .orElseThrow(NotFoundException::new));
            }
            notification.setCreatedBy(user);
            notification = notificationRepository.save(notification);
            Iterator<NotificationMobile> iNotificationMobiles = notification.getUserMobiles().iterator();
            while (iNotificationMobiles.hasNext()) {
                NotificationMobile tempNotificationMobile = iNotificationMobiles.next();
                tempNotificationMobile.setNotificationId(notification.getId());
                notificationMobileRepository.save(tempNotificationMobile);
            }
            return notification;
        } else {
            throw new ForbiddenException();
        }
    }

    @GetMapping("/{processId}/notifications/{notificationId}")
    Notification getProcessNotification(@PathVariable(name = "processId") Long processId,
            @PathVariable(name = "notificationId") Long notificationId, Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        if (isProcessUser(sso, processId)) {
            return notificationRepository.findById(notificationId).orElseThrow(NotFoundException::new);
        } else {
            throw new ForbiddenException();
        }
    }

    @PutMapping("/{processId}/notifications/{notificationId}")
    Notification updateNotification(@PathVariable(name = "processId") Long processId,
            @PathVariable(name = "notificationId") Long notificationId, @RequestBody Notification notification,
            Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        User user = userRepository.findFirstBySso(sso);

        if (isProcessUser(sso, processId)) {
            if (notificationRepository.existsById(notificationId)) {
                notification.setId(notificationId);
                notification.setProcessId(processId);
                notification.setCreatedBy(user);
                notificationMobileRepository.deleteByNotificationId(notificationId);
                Iterator<NotificationMobile> iNotificationMobiles = notification.getUserMobiles().iterator();
                while (iNotificationMobiles.hasNext()) {
                    NotificationMobile tempNotificationMobile = iNotificationMobiles.next();
                    notificationMobileRepository.save(tempNotificationMobile);
                }
                return notificationRepository.save(notification);
            } else {
                throw new NotFoundException();
            }
        } else {
            throw new ForbiddenException();
        }
    }

    @DeleteMapping("/{processId}/notifications/{notificationId}")
    Notification deleteNotification(@PathVariable(name = "processId") Long processId,
            @PathVariable(name = "notificationId") Long notificationId, Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        if (isProcessUser(sso, processId)) {
            Notification notification = notificationRepository.findById(notificationId)
                    .orElseThrow(NotFoundException::new);
            notificationRepository.delete(notification);
            return notification;
        } else {
            throw new ForbiddenException();
        }
    }

    @GetMapping("/{processId}/schedule-definitions")
    List<ScheduleDefinition> getScheduleDefinitions(@PathVariable(name = "processId") Long processId,
            Authentication authentication) {
        return scheduleDefinitionRepository.findByProcessId(processId);
    }

    @GetMapping("/{processId}/schedule-definitions/all")
    List<ScheduleDefinition> getScheduleDefinitionsAll(@PathVariable(name = "processId") Long processId) {
        return scheduleDefinitionRepository.findByProcessId(processId);
    }

    @PostMapping("/{processId}/schedule-definitions")
    ScheduleDefinition createScheduleDefinition(@PathVariable(name = "processId") Long processId,
            @RequestBody ScheduleDefinitionDTO scheduleDefinitionRequest, Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        if (isProcessUser(sso, processId)) {
            ScheduleDefinition scheduleDefinition = new ScheduleDefinition();
            assignScheduleDefFields(scheduleDefinition, scheduleDefinitionRequest, processId, authentication);
            scheduleDefinition = scheduleDefinitionRepository.save(scheduleDefinition);
            scheduledSubmissionService.setupScheduledSubmissions(scheduleDefinition, scheduleDefinitionRequest);
            return scheduleDefinition;
        } else {
            throw new ForbiddenException();
        }
    }

    @PutMapping("/{processId}/schedule-definitions/{scheduleDefinitionId}")
    @Transactional
    ScheduleDefinition updateScheduleDefinition(@PathVariable(name = "processId") Long processId,
            @PathVariable(name = "scheduleDefinitionId") Long scheduleDefinitionId,
            @RequestBody ScheduleDefinitionDTO scheduleDefinitionRequest, Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        if (isProcessUser(sso, processId)
                && existsByScheduleDefinitionIdAndProcessId(scheduleDefinitionId, processId)) {
            ScheduleDefinition scheduleDefinition = this.scheduleDefinitionRepository.findById(scheduleDefinitionId)
                    .get();
            assignScheduleDefFields(scheduleDefinition, scheduleDefinitionRequest, processId, authentication);
            scheduleDefinition = scheduleDefinitionRepository.save(scheduleDefinition);
            scheduledSubmissionRepository.deleteByScheduleDefinitionId(scheduleDefinitionId);
            scheduledSubmissionService.setupScheduledSubmissions(scheduleDefinition, scheduleDefinitionRequest);
            return scheduleDefinition;
        } else {
            throw new ForbiddenException();
        }
    }

    @GetMapping("/{processId}/schedule-definitions/{scheduleDefinitionId}")
    ScheduleDefinition getScheduleDefinition(@PathVariable(name = "processId") Long processId,
            @PathVariable(name = "scheduleDefinitionId") Long scheduleDefinitionId, Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        ScheduleDefinition sched = scheduleDefinitionRepository.findById(scheduleDefinitionId)
                .orElseThrow(NotFoundException::new);
        if (sched != null && isProcessUser(sso, sched.getProcess().getId())) {
            return sched;
        } else {
            throw new NotFoundException();
        }
    }

    @DeleteMapping("/{processId}/schedule-definitions/{scheduleDefinitionId}")
    @Transactional
    public ScheduleDefinition deleteScheduleDefinition(@PathVariable(name = "processId") Long processId,
            @PathVariable(name = "scheduleDefinitionId") Long scheduleDefinitionId, Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        if (isProcessUser(sso, processId)
                && existsByScheduleDefinitionIdAndProcessId(scheduleDefinitionId, processId)) {
            ScheduleDefinition scheduleDefinition = scheduleDefinitionRepository.findById(scheduleDefinitionId)
                    .orElseThrow(NotFoundException::new);
            scheduledSubmissionRepository.deleteByScheduleDefinitionId(scheduleDefinitionId);
            scheduleDefinitionRepository.delete(scheduleDefinition);
            return scheduleDefinition;
        } else {
            throw new ForbiddenException();
        }
    }

    @PutMapping("/{processId}/alerts")
    Process updateAlerts(@PathVariable(value = "processId") Long processId, @RequestBody ProcessAlert processAlert,
            Authentication authentication) {
        Process process = processRepository.findById(processId).get();
        Long sso = (Long) authentication.getPrincipal();
        User appOwner = getAppOwner(process);
        if (!(appOwner.getSso().equals(sso) || isAdmin(authentication.getAuthorities()))) {
            throw new ConflictException("Automated notifications can be configured either by Admin or App Owner.");
        }
        if (processRepository.existsById(processId)) {
            process.setLongRunningSubAlrt(processAlert.getLongRunningSubAlrt());
            process.setSubmissionEscalationAlrt(processAlert.getSubmissionEscalationAlrt());
            process.setLongRunningStepAlrt(processAlert.getLongRunningStepAlrt());
            process.setSubmissionDelayedEscalationAlrt(processAlert.getSubmissionDelayedEscalationAlrt());
            process.setRequiredStepAlrt(processAlert.getRequiredStepAlrt());
            return processRepository.saveAndFlush(process);
        } else {
            throw new NotFoundException();
        }
    }

    @PostMapping("/{processId}/copy")
    @PreAuthorize("hasAnyAuthority('admin', 'application')")
    Process copyProcess(@PathVariable("processId") Long processId, @RequestBody ProcessCopyRequest processCopyRequest,
            Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        if (isAdmin(authentication.getAuthorities()) || isProcessUser(sso, processId)) {
            Process processToCopy = processRepository.findById(processId).orElseThrow(NotFoundException::new);
            Process newProcess = processImportService.importProcessWithName(processToCopy,
                    processCopyRequest.getName());
            for (String setting : processCopyRequest.getSettings()) {
                switch (setting) {
                case "steps":
                    processImportService.importProcessSteps(processStepRepository.findByProcessId(processId),
                            newProcess.getId());
                    break;
                case "notifications":
                    processImportService.importNotifications(notificationRepository.findByProcessId(processId),
                            newProcess.getId());
                    break;
                case "users":
                    processImportService.importUsers(processUserRepository.findByProcessId(processId).stream()
                            .map(ProcessUser::getUser).collect(Collectors.toList()), newProcess.getId());
                    break;
                case "schedules":
                    processImportService.importScheduleDefinitions(
                            scheduleDefinitionRepository.findByProcessId(processId), newProcess.getId());
                    break;
                default:
                    break;
                }
            }
            return newProcess;
        } else {
            throw new ForbiddenException();
        }
    }

    @PostMapping("/{processId}/import")
    @PreAuthorize("hasAnyAuthority('admin', 'application')")
    Process importProcess(@PathVariable("processId") Long processId, @RequestBody ProcessExport processExport) {
        Process process = processExport.getProcess() != null
                ? processImportService.importProcess(processExport.getProcess())
                : processRepository.findById(processId).orElseThrow(NotFoundException::new);

        if (!process.getIsParent() && processExport.getProcessChild() != null) {
            for (ProcessParentChild allData : processExport.getProcessChild()) {
                if (allData.getProcessChild().getName().trim().equalsIgnoreCase(process.getName().trim())) {
                    processImportService.importProcessParentChild(allData, process.getId());
                }
            }
        }
        if (processExport.getSteps() != null) {
            processImportService.importProcessSteps(processExport.getSteps(), process.getId());
        }
        if (processExport.getNotifications() != null) {
            processImportService.importNotifications(processExport.getNotifications(), process.getId());
        }
        if (processExport.getUsers() != null) {
            processImportService.importUsers(processExport.getUsers(), process.getId());
        }
        if (processExport.getSchedules() != null) {
            processImportService.importScheduleDefinitions(processExport.getSchedules(), process.getId());
        }
        List<String> adminEmails = userRepository.findUserByRoleName("admin").stream().map(User::getEmail)
                .collect(Collectors.toList());
        if (processExport.getRequester() != null) {
            adminEmails.add(processExport.getRequester().getEmail());
        }
        String to = String.join(" ", adminEmails);
        EmailModel emailModel = new EmailModel(NotificationTemplate.PROCESS_IMPORTED, to, false);
        emailModel.process = process;
        spotlightEmailService.genericSend(emailModel);
        return process;
    }

    @GetMapping("/{processId}/exports")
    @PreAuthorize("hasAnyAuthority('admin', 'application')")
    Collection<ProcessExportRequest> getExportRequests(@PathVariable("processId") Long processId,
            Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        if (isAdmin(authentication.getAuthorities())) {
            return processExportRequestRepository.findByProcessId(processId);
        } else if (isProcessUser(sso, processId)) {
            User user = userRepository.findFirstBySso(sso);
            return processExportRequestRepository.findByProcessIdAndUserId(processId, user.getId());
        } else {
            throw new ForbiddenException();
        }
    }

    @PostMapping("/{processId}/exports")
    @PreAuthorize("hasAnyAuthority('admin', 'application')")
    ProcessExportRequest createExportRequest(@PathVariable("processId") Long processId,
            @RequestBody ProcessExportRequestRequest processExportRequest, Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        if (isAdmin(authentication.getAuthorities()) || isProcessUser(sso, processId)) {
            User user = userRepository.findFirstBySso(sso);
            Process process = processRepository.findById(processId).orElseThrow(NotFoundException::new);
            ProcessExportRequest request = new ProcessExportRequest();
            request.setSettings(processExportRequest.getSettings());
            request.setProcessId(processId);
            request.setUserId(user.getId());
            request.setState(isAdmin(authentication.getAuthorities()) ? ProcessExportRequest.State.ACCEPTED
                    : ProcessExportRequest.State.REQUESTED);
            request.setRequested(new Date());
            request = processExportRequestRepository.save(request);
            if (!isAdmin(authentication.getAuthorities())) {
                List<String> adminEmails = userRepository.findUserByRoleName("admin").stream().map(User::getEmail)
                        .collect(Collectors.toList());
                String to = String.join(" ", adminEmails);
                EmailModel emailModel = new EmailModel(NotificationTemplate.PROCESS_EXPORT_REQUEST, to, false);
                emailModel.process = process;
                emailModel.user = user;
                spotlightEmailService.genericSend(emailModel);
            }
            return request;
        } else {
            throw new ForbiddenException();
        }
    }

    @GetMapping("/{processId}/exports/{exportRequestId}")
    @PreAuthorize("hasAnyAuthority('admin', 'application')")
    ProcessExportRequest getExportRequest(@PathVariable("processId") Long processId,
            @PathVariable("exportRequestId") Long exportRequestId, Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        if (isAdmin(authentication.getAuthorities())) {
            return processExportRequestRepository.findFirsByIdAndProcessId(exportRequestId, processId)
                    .orElseThrow(NotFoundException::new);
        } else {
            User user = userRepository.findFirstBySso(sso);
            return processExportRequestRepository
                    .findFirstByIdAndProcessIdAndUserId(exportRequestId, processId, user.getId())
                    .orElseThrow(NotFoundException::new);
        }
    }

    @GetMapping("/{processId}/exports/{exportRequestId}/export")
    @PreAuthorize("hasAnyAuthority('admin', 'application')")
    ProcessExport getProcessExport(@PathVariable("processId") Long processId,
            @PathVariable("exportRequestId") Long exportRequestId, Authentication authentication,
            HttpServletResponse response) {
        Long sso = (Long) authentication.getPrincipal();
        if (isAdmin(authentication.getAuthorities()) || isProcessUser(sso, processId)) {
            ProcessExportRequest request = processExportRequestRepository
                    .findFirsByIdAndProcessId(exportRequestId, processId).orElseThrow(NotFoundException::new);
            if (request.getState() == ProcessExportRequest.State.ACCEPTED) {
                ProcessExport export = new ProcessExport();
                for (String setting : request.getSettings()) {
                    switch (setting) {
                    case "summary":
                        Process process = processRepository.findById(processId).orElseThrow(NotFoundException::new);
                        if (!process.getIsParent() && process.getProcessParent() != null) {
                            export.setProcessChild(processParentChildRepository.findByChildId(process.getId()));
                        }
                        export.setProcess(process);
                        break;
                    case "steps":
                        export.setSteps(processStepRepository.findByProcessId(processId));
                        break;
                    case "notifications":
                        export.setNotifications(notificationRepository.findByProcessId(processId));
                        break;
                    case "users":
                        export.setUsers(processUserRepository.findByProcessId(processId).stream()
                                .map(ProcessUser::getUser).collect(Collectors.toList()));
                        break;
                    case "schedules":
                        export.setSchedules(scheduleDefinitionRepository.findByProcessId(processId));
                        break;
                    default:
                        break;
                    }
                }
                export.setRequester(userRepository.findById(request.getUserId()).orElseThrow(NotFoundException::new));
                return export;
            } else {
                throw new ConflictException("Export request must be accepted.");
            }
        } else {
            throw new ForbiddenException();
        }
    }

    @PatchMapping("/{processId}/exports/{exportRequestId}/approve")
    @PreAuthorize("hasAuthority('admin')")
    ProcessExportRequest acceptExportRequest(@PathVariable("processId") Long processId,
            @PathVariable("exportRequestId") Long exportRequestId,
            @RequestBody ProcessExportNotesRequest processExportNotesRequest) {
        ProcessExportRequest request = processExportRequestRepository
                .findFirsByIdAndProcessId(exportRequestId, processId).orElseThrow(NotFoundException::new);
        if (request.getState() == ProcessExportRequest.State.REQUESTED) {
            User user = userRepository.findById(request.getUserId()).orElseThrow(NotFoundException::new);
            Process process = processRepository.findById(processId).orElseThrow(NotFoundException::new);
            request.setState(ProcessExportRequest.State.ACCEPTED);
            request.setNotes(processExportNotesRequest.getNotes());
            request = processExportRequestRepository.save(request);
            spotlightEmailService.sendProcessExportDecision(NotificationTemplate.PROCESS_EXPORT_DECISION,
                    user.getEmail(), process, user, request);
            return request;
        } else {
            throw new ConflictException("Only state 'Requested' requests can be accepted.");
        }
    }

    @PatchMapping("/{processId}/exports/{exportRequestId}/decline")
    @PreAuthorize("hasAuthority('admin')")
    ProcessExportRequest declineExportRequest(@PathVariable("processId") Long processId,
            @PathVariable("exportRequestId") Long exportRequestId,
            @RequestBody ProcessExportNotesRequest processExportNotesRequest) {
        ProcessExportRequest request = processExportRequestRepository
                .findFirsByIdAndProcessId(exportRequestId, processId).orElseThrow(NotFoundException::new);
        if (request.getState() == ProcessExportRequest.State.REQUESTED) {
            User user = userRepository.findById(request.getUserId()).orElseThrow(NotFoundException::new);
            Process process = processRepository.findById(processId).orElseThrow(NotFoundException::new);
            request.setState(ProcessExportRequest.State.DECLINED);
            request.setNotes(processExportNotesRequest.getNotes());
            request = processExportRequestRepository.save(request);
            spotlightEmailService.sendProcessExportDecision(NotificationTemplate.PROCESS_EXPORT_DECISION,
                    user.getEmail(), process, user, request);
            return request;
        } else {
            throw new ConflictException("Only state 'Requested' requests can be declineds.");
        }
    }

    private void assignScheduleDefFields(ScheduleDefinition scheduleDefinition,
            ScheduleDefinitionDTO scheduleDefinitionRequest, long processId, Authentication authentication) {
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            // isoFormat.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
            Date fromDate = null;
            Date toDate = null;
            Long sso = (Long) authentication.getPrincipal();
            User user = userRepository.findFirstBySso(sso);
            if ((scheduleDefinitionRequest.getIsPredecessorEndTime()
                    || scheduleDefinition.getRecurrencePattern() == 'E') && scheduleDefinition.getId() == null) {
                Calendar today = Calendar.getInstance();
                today.setTime(new Date());
                today.add(Calendar.DATE, 30);
                Date end = today.getTime();
                Date now = new Date();

                List<Long> processIdList = processUserRepository.findByUserId(user.getId()).stream()
                        .map(ProcessUser::getProcessId).collect(Collectors.toList());

                List<ScheduledSubmission> scheduledSubmissions = scheduledSubmissionRepository
                        .findByStartTimeIsBetweenAndProcessIdIsInOrderByEndTimeAsc(now, end, processIdList);

                if (!scheduledSubmissions.isEmpty()
                        && !scheduledSubmissions.get(0).getProcess().getId().equals(processId)) {

                    Calendar scheduleEndTime = Calendar.getInstance();
                    scheduleEndTime.setTime(scheduledSubmissions.get(0).getEndTime());
                    scheduleEndTime.add(Calendar.MINUTE, scheduleDefinitionRequest.getDuration().intValue());

                    scheduleDefinition.setStartTime(scheduledSubmissions.get(0).getEndTime());
                    scheduleDefinition.setEndTime(scheduleEndTime.getTime());
                    scheduleDefinition.setScheduleStartDate(scheduledSubmissions.get(0).getEndTime());
                    scheduleDefinition.setScheduleEndDate(scheduleEndTime.getTime());
                    scheduleDefinition
                            .setProcess(processRepository.findById(processId).orElseThrow(NotFoundException::new));
                    scheduleDefinition.setRecurrencePattern(scheduleDefinitionRequest.getRecurrencePattern());
                    scheduleDefinition.setTolerance(scheduleDefinitionRequest.getDuration().intValue());
                    scheduleDefinition.setPredecessorSubmissionId(scheduledSubmissions.get(0).getId());
                    scheduleDefinitionRequest.setPredecessorScheduleSubmissionId(scheduledSubmissions.get(0).getId());
                } else {
                    throw new RuntimeException("Preceding processes and the process selected cannot be same.");
                }
            } else if ((scheduleDefinitionRequest.getIsPredecessorEndTime()
                    || scheduleDefinition.getRecurrencePattern() == 'E') && scheduleDefinition.getId() != null) {

                Calendar scheduleEndTime = Calendar.getInstance();
                scheduleEndTime.setTime(scheduleDefinition.getStartTime());
                scheduleEndTime.add(Calendar.MINUTE, scheduleDefinitionRequest.getDuration().intValue());

                scheduleDefinition.setEndTime(scheduleEndTime.getTime());
                scheduleDefinition.setScheduleEndDate(scheduleEndTime.getTime());
                scheduleDefinition.setTolerance(scheduleDefinitionRequest.getDuration().intValue());

            } else {
                fromDate = isoFormat.parse(scheduleDefinitionRequest.getStartTime());
                toDate = isoFormat.parse(scheduleDefinitionRequest.getEndTime());
                scheduleDefinition.setEndTime(toDate);
                scheduleDefinition
                        .setProcess(processRepository.findById(processId).orElseThrow(NotFoundException::new));
                scheduleDefinition.setProcessWorkflowId(scheduleDefinitionRequest.getProcessWorkflowId());
                scheduleDefinition.setRecurrencePattern(scheduleDefinitionRequest.getRecurrencePattern());
                scheduleDefinition.setRecurrenceTime(scheduleDefinitionRequest.getRecurrenceTime());
                scheduleDefinition.setScheduleChangeFlag(scheduleDefinitionRequest.getScheduleChangeFlag());
                scheduleDefinition.setScheduleEndDate(scheduleDefinitionRequest.getScheduleEndDate());
                scheduleDefinition.setScheduleStartDate(scheduleDefinitionRequest.getScheduleStartDate());
                scheduleDefinition.setSettings(scheduleDefinitionRequest.getSettings());
                scheduleDefinition.setStartTime(fromDate);
                scheduleDefinition.setTolerance(scheduleDefinitionRequest.getTolerance());
            }
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format");
        }

    }

    @PostMapping("/{processId}/schedule-critical-definitions")
    ScheduleCriticalDefinition createScheduleCriticalDefinition(@PathVariable(name = "processId") Long processId,
            @RequestBody ScheduleDefinitionDTO scheduleDefinitionRequest, Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        if (isProcessUser(sso, processId)) {
            ScheduleCriticalDefinition scheduleCriticalDefinition = new ScheduleCriticalDefinition();
            assignScheduleCriticalDefFields(scheduleCriticalDefinition, scheduleDefinitionRequest, processId);
            scheduleCriticalDefinition = scheduleCriticalDefinitionRepository.save(scheduleCriticalDefinition);
            return scheduleCriticalDefinition;
        } else {
            throw new ForbiddenException();
        }
    }

    private void assignScheduleCriticalDefFields(ScheduleCriticalDefinition scheduleCriticalDefinition,
            ScheduleDefinitionDTO scheduleDefinitionRequest, long processId) {
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            isoFormat.setTimeZone(TimeZone.getTimeZone("US/Eastern"));

            scheduleCriticalDefinition
                    .setProcess(processRepository.findById(processId).orElseThrow(NotFoundException::new));
            scheduleCriticalDefinition.setRecurrencePattern(scheduleDefinitionRequest.getRecurrencePattern());
            scheduleCriticalDefinition.setScheduleChangeFlag(scheduleDefinitionRequest.getScheduleChangeFlag());
            scheduleCriticalDefinition.setScheduleEndDate(scheduleDefinitionRequest.getScheduleEndDate());
            scheduleCriticalDefinition.setScheduleStartDate(scheduleDefinitionRequest.getScheduleStartDate());
            scheduleCriticalDefinition.setSettings(scheduleDefinitionRequest.getSettings());
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format");
        }

    }

    @DeleteMapping("/{processId}/schedule-critical-definitions/{scheduleCriticalDefinitionId}")
    @Transactional
    public ScheduleCriticalDefinition deleteScheduleCriticalDefinition(@PathVariable(name = "processId") Long processId,
            @PathVariable(name = "scheduleCriticalDefinitionId") Long scheduleDefinitionId,
            Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        if (isProcessUser(sso, processId)
                && existsByScheduleCriticalDefinitionIdAndProcessId(scheduleDefinitionId, processId)) {
            ScheduleCriticalDefinition scheduleCriticalDefinition = scheduleCriticalDefinitionRepository
                    .findById(scheduleDefinitionId).orElseThrow(NotFoundException::new);
            scheduleCriticalDefinitionRepository.delete(scheduleCriticalDefinition);
            return scheduleCriticalDefinition;
        } else {
            throw new ForbiddenException();
        }
    }

    @GetMapping("/{processId}/schedule-critical-definitions/{scheduleCriticalDefinitionId}")
    ScheduleCriticalDefinition getScheduleCriticalDefinition(@PathVariable(name = "processId") Long processId,
            @PathVariable(name = "scheduleCriticalDefinitionId") Long scheduleDefinitionId,
            Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        ScheduleCriticalDefinition sched = scheduleCriticalDefinitionRepository.findById(scheduleDefinitionId)
                .orElseThrow(NotFoundException::new);
        if (sched != null && isProcessUser(sso, sched.getProcess().getId())) {
            return sched;
        } else {
            throw new NotFoundException();
        }
    }

    @PutMapping("/{processId}/schedule-critical-definitions/{scheduleCriticalDefinitionId}")
    @Transactional
    ScheduleCriticalDefinition updateScheduleCriticalDefinition(@PathVariable(name = "processId") Long processId,
            @PathVariable(name = "scheduleCriticalDefinitionId") Long scheduleDefinitionId,
            @RequestBody ScheduleDefinitionDTO scheduleDefinitionRequest, Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        if (isProcessUser(sso, processId)
                && existsByScheduleCriticalDefinitionIdAndProcessId(scheduleDefinitionId, processId)) {
            ScheduleCriticalDefinition scheduleDefinition = this.scheduleCriticalDefinitionRepository
                    .findById(scheduleDefinitionId).get();
            assignScheduleCriticalDefFields(scheduleDefinition, scheduleDefinitionRequest, processId);
            scheduleDefinition = scheduleCriticalDefinitionRepository.save(scheduleDefinition);
            return scheduleDefinition;
        } else {
            throw new ForbiddenException();
        }
    }

    @GetMapping("/{processId}/schedule-critical-definitions")
    List<ScheduleCriticalDefinition> getScheduleCriticalDefinitions(@PathVariable(name = "processId") Long processId) {
        return scheduleCriticalDefinitionRepository.findByProcessId(processId);
    }

    @GetMapping("/{processId}/schedule-critical-definitions/all")
    List<ScheduleCriticalDefinition> getScheduleCriticalDefinitionsAll(
            @PathVariable(name = "processId") Long processId) {
        return scheduleCriticalDefinitionRepository.findByProcessId(processId);
    }

}