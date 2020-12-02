package com.ge.finance.spotlight.controllers;

import com.auth0.jwt.JWT;
import com.ge.finance.spotlight.dto.ProcessApprovalDTO;
import com.ge.finance.spotlight.dto.ProcessDTO;
import com.ge.finance.spotlight.exceptions.ConflictException;
import com.ge.finance.spotlight.exceptions.ForbiddenException;
import com.ge.finance.spotlight.exceptions.NotFoundException;
import com.ge.finance.spotlight.models.*;
import com.ge.finance.spotlight.models.Process;
import com.ge.finance.spotlight.repositories.*;
import com.ge.finance.spotlight.models.ProcessUser;
import com.ge.finance.spotlight.requests.ProcessUserRequest;
import com.ge.finance.spotlight.services.ScheduledSubmissionService;
import com.ge.finance.spotlight.services.SpotlightEmailService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

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
    private SpotlightEmailService spotlightEmailService;
    private RoleRepository roleRepository;
    private ProcessParentChildRepository processParentChildRepository;
    private SubmissionStepRepository submissionStepRepository;

    public ProcessController(ProcessRepository processRepository, ProcessStepRepository processStepRepository,
            UserRepository userRepository, ProcessUserRepository processUserRepository,
            NotificationRepository notificationRepository, StatusRepository statusRepository,
            ScheduleDefinitionRepository scheduleDefinitionRepository,
            ScheduledSubmissionService scheduledSubmissionService,
            ScheduledSubmissionRepository scheduledSubmissionRepository,
            SpotlightEmailService spotlightEmailService, RoleRepository roleRepository, ProcessParentChildRepository processParentChildRepository,
            SubmissionStepRepository submissionStepRepository) {
        this.processRepository = processRepository;
        this.processStepRepository = processStepRepository;
        this.userRepository = userRepository;
        this.processUserRepository = processUserRepository;
        this.notificationRepository = notificationRepository;
        this.statusRepository = statusRepository;
        this.scheduleDefinitionRepository = scheduleDefinitionRepository;
        this.scheduledSubmissionService = scheduledSubmissionService;
        this.scheduledSubmissionRepository = scheduledSubmissionRepository;
        this.spotlightEmailService = spotlightEmailService;
        this.roleRepository = roleRepository;
        this.processParentChildRepository = processParentChildRepository;
        this.submissionStepRepository = submissionStepRepository;
    }

    private boolean isAdmin(Collection<? extends GrantedAuthority> grantedAuthorities) {
        for (GrantedAuthority grantedAuthority : grantedAuthorities) {
            if (grantedAuthority.getAuthority().equals("admin")) {
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

    @GetMapping("/")
    List<Process> index(Authentication authentication) {
        if (isAdmin(authentication.getAuthorities())) {
            return processRepository.findAll();
        } else {
            Long sso = (Long) authentication.getPrincipal();
            User user = userRepository.findFirstBySso(sso);
            List<Long> processIdList = processUserRepository.findByUserId(user.getId()).stream()
                    .map(ProcessUser::getProcessId).collect(Collectors.toList());
            return processRepository.findByIdIsIn(processIdList);
        }
    }

    @GetMapping("/all")
    List<Process> indexAll() {
        return processRepository.findAll();
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
            this.spotlightEmailService.genericSend(NotificationTemplate.PROCESS_APPROVAL,
                    "jamie.myers@ge.com SpotlightDevTeam@ge.com", null, null, process, requestor, null, null, null);
        }

        createDefaultProcessSteps(process.getId());
        return process;
    }

    private void createDefaultProcessSteps(Long processId) {
        ProcessStep start = new ProcessStep();
        start.setProcessId(processId);
        start.setName(ProcessStep.START);
        processStepRepository.save(start);
        ProcessStep end = new ProcessStep();
        end.setProcessId(processId);
        end.setName(ProcessStep.END);
        processStepRepository.save(end);
    }

    @GetMapping("/{processId}")
    Process get(@PathVariable(value = "processId") Long processId, Authentication authentication) {
        if (isAdmin(authentication.getAuthorities())) {
            return processRepository.findById(processId).orElseThrow(NotFoundException::new);
        } else {
            Long sso = (Long) authentication.getPrincipal();
            if (isProcessUser(sso, processId)) {
                return processRepository.findById(processId).orElseThrow(NotFoundException::new);
            } else {
                throw new ForbiddenException();
            }
        }
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
    List<Process> getChildren(@PathVariable(value = "processId") Long processId, Authentication authentication) {
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
        if (processRepository.countByProcessParentId(process.getId()) == 0
                && processStepRepository.countByProcessId(processId) == 0
                && processUserRepository.countByProcessId(processId) == 0) {
            processRepository.delete(process);
            return process;
        } else {
            throw new ConflictException();
        }
    }

    @GetMapping("/{processId}/steps")
    List<ProcessStep> indexSteps(@PathVariable(value = "processId") Long processId, Authentication authentication) {
        if (isAdmin(authentication.getAuthorities())) {
            return processStepRepository.findByProcessId(processId);
        } else {
            Long sso = (Long) authentication.getPrincipal();
            if (isProcessUser(sso, processId)) {
                return processStepRepository.findByProcessId(processId);
            } else {
                throw new ForbiddenException();
            }
        }
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
            spotlightEmailService.genericSend(NotificationTemplate.PROCESS_APPROVAL_RESULT,
                    ("jamie.myers@ge.com SpotlightDevTeam@ge.com" + " " + requestorEmail), null, null, process,
                    requestor, null, null, null);
            return process;
        } else {
            throw new ForbiddenException();
        }
    }

    @PostMapping("/{processId}/steps")
    ProcessStep createStep(@PathVariable(value = "processId") Long processId, @RequestBody ProcessStep processStep,
            Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        if (processRepository.existsById(processId)) {
            if (isProcessUser(sso, processId)) {
                processStep.setProcessId(processId);
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
        if (isProcessUser(sso, processId) && processStepRepository.existsByIdAndProcessId(processStepId, processId)) {
            processStep.setId(processStepId);
            return processStepRepository.save(processStep);
        } else {
            throw new ForbiddenException();
        }
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
    ProcessUser addUserToProcess(@PathVariable(value = "processId") Long processId,
            @RequestBody ProcessUserRequest processUserRequest, Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        if (isAdmin(authentication.getAuthorities()) || isProcessUser(sso, processId)) {
            return newProcessUser(processId, processUserRequest.getUserId());
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
    ProcessParentChild addChildren(@PathVariable Long processId, @RequestBody ProcessParentChild processParentChild,
            Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        if (isProcessUser(sso, processId)) {
            return this.processParentChildRepository.save(processParentChild);
        } else {
            throw new ForbiddenException();
        }

    }

    @DeleteMapping("/{processId}/children-map/{childId}")
    void removeChildren(@PathVariable Long processId, @PathVariable Long childId, Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        if (isProcessUser(sso, processId)) {
            try {
                this.processParentChildRepository.deleteById(childId);
            } catch (Exception ex) {
                throw new RuntimeException(ex.getMessage());
            }
        } else {
            throw new ForbiddenException();
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
            return notificationRepository.save(notification);
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
        if (isProcessUser(sso, processId)) {
            if (notificationRepository.existsById(notificationId)) {
                notification.setId(notificationId);
                notification.setProcessId(processId);
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
        Long sso = (Long) authentication.getPrincipal();
        if (isAdmin(authentication.getAuthorities()) || isProcessUser(sso, processId)) {
            return scheduleDefinitionRepository.findByProcessId(processId);
        } else {
            throw new ForbiddenException();
        }
    }

    @GetMapping("/{processId}/schedule-definitions/all")
    List<ScheduleDefinition> getScheduleDefinitionsAll(@PathVariable(name = "processId") Long processId) {
        return scheduleDefinitionRepository.findByProcessId(processId);
    }

    @PostMapping("/{processId}/schedule-definitions")
    ScheduleDefinition createScheduleDefinition(@PathVariable(name = "processId") Long processId,
            @RequestBody ScheduleDefinition scheduleDefinition, Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        if (isProcessUser(sso, processId)) {
            scheduleDefinition.setProcess(processRepository.findById(processId).orElseThrow(NotFoundException::new));
            scheduleDefinition = scheduleDefinitionRepository.save(scheduleDefinition);
            scheduledSubmissionService.setupScheduledSubmissions(scheduleDefinition);
            return scheduleDefinition;
        } else {
            throw new ForbiddenException();
        }
    }

    @PutMapping("/{processId}/schedule-definitions/{scheduleDefinitionId}")
    @Transactional
    ScheduleDefinition updateScheduleDefinition(@PathVariable(name = "processId") Long processId,
            @PathVariable(name = "scheduleDefinitionId") Long scheduleDefinitionId,
            @RequestBody ScheduleDefinition scheduleDefinition, Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        if (isProcessUser(sso, processId)
                && existsByScheduleDefinitionIdAndProcessId(scheduleDefinitionId, processId)) {
            scheduleDefinition.setId(scheduleDefinitionId);
            scheduleDefinition.setProcess(processRepository.findById(processId).orElseThrow(NotFoundException::new));
            scheduleDefinition = scheduleDefinitionRepository.save(scheduleDefinition);
            scheduledSubmissionRepository.deleteByScheduleDefinitionId(scheduleDefinitionId);
            scheduledSubmissionService.setupScheduledSubmissions(scheduleDefinition);
            return scheduleDefinition;
        } else {
            throw new ForbiddenException();
        }
    }

    @GetMapping("/{processId}//schedule-definitions/{scheduleDefinitionId}")
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

        if (!isAdmin(authentication.getAuthorities())) {
            throw new ForbiddenException();
        }

        if (processRepository.existsById(processId)) {
            Process process = processRepository.findById(processId).get();
            process.setLongRunningSubAlrt(processAlert.getLongRunningSubmission());
            process.setSubmissionEscalationAlrt(processAlert.getFailedEscalation());
            process.setLongRunningStepAlrt(processAlert.getLongRunningStep());
            return processRepository.saveAndFlush(process);
        } else {
            throw new NotFoundException();
        }
    }
}