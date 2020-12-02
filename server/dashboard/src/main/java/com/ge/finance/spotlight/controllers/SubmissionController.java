package com.ge.finance.spotlight.controllers;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.auth0.jwt.JWT;
import com.ge.finance.spotlight.dto.AcknowledgeDTO;
import com.ge.finance.spotlight.exceptions.NotFoundException;
import com.ge.finance.spotlight.filters.FilterBuilder;
import com.ge.finance.spotlight.libs.AcknowledgeLib;
import com.ge.finance.spotlight.libs2.ManualTouchpoint;
import com.ge.finance.spotlight.models.NotificationTemplate;
import com.ge.finance.spotlight.models.Process;
import com.ge.finance.spotlight.models.ProcessUser;
import com.ge.finance.spotlight.models.Submission;
import com.ge.finance.spotlight.models.SubmissionStep;
import com.ge.finance.spotlight.models.User;
import com.ge.finance.spotlight.repositories.ProcessRepository;
import com.ge.finance.spotlight.repositories.ProcessUserRepository;
import com.ge.finance.spotlight.repositories.SubmissionRepository;
import com.ge.finance.spotlight.repositories.SubmissionStepRepository;
import com.ge.finance.spotlight.repositories.UserRepository;
import com.ge.finance.spotlight.requests.ManualSubmissionStepRequest;
import com.ge.finance.spotlight.services.SpotlightEmailService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/submissions")
public class SubmissionController {

    @Value("${app_secret}")
    private String app_secret;

    private SubmissionRepository submissionRepository;
    private SubmissionStepRepository submissionStepRepository;
    private ProcessRepository processRepository;
    private UserRepository userRepository;
    private ProcessUserRepository processUserRepository;
    private SpotlightEmailService spotlightEmailService;

    private List<String> allowedProcessFilters = Arrays.asList("sender", "receiver", "processType", "childId");
    private List<String> allowedSubmissionFilters = Arrays.asList("id", "from", "to", "status", "bu", "altId", "adHoc");
    private List<String> allowedPaginationFilters = Arrays.asList("size", "page", "sortField", "sortOrder");

    public SubmissionController(SubmissionRepository submissionRepository,
            SubmissionStepRepository submissionStepRepository, ProcessRepository processRepository,
            UserRepository userRepository, ProcessUserRepository processUserRepository,
            SpotlightEmailService spotlightEmailService) {
        this.submissionRepository = submissionRepository;
        this.submissionStepRepository = submissionStepRepository;
        this.processRepository = processRepository;
        this.userRepository = userRepository;
        this.processUserRepository = processUserRepository;
        this.spotlightEmailService = spotlightEmailService;
    }

    private boolean isProcessUser(Long sso, Long processId) {
        User user = userRepository.findFirstBySso(sso);
        List<ProcessUser> processUsers = processUserRepository.findByUserIdAndProcessId(user.getId(), processId);
        return !processUsers.isEmpty();
    }

    @GetMapping("/")
    public Page<Submission> index(@RequestParam Map<String, String> filters, Authentication authentication) {
        Pageable pageList;

        Map<String, String> processFilters = filters.entrySet().stream()
                .filter(entry -> allowedProcessFilters.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<String, String> submissionFilters = filters.entrySet().stream()
                .filter(entry -> allowedSubmissionFilters.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<String, String> paginationFilters = filters.entrySet().stream()
                .filter(entry -> allowedPaginationFilters.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        int page = Integer.valueOf(paginationFilters.get("page") == null ? "0" : paginationFilters.get("page"));
        int pageSize = Integer.valueOf(paginationFilters.get("size") == null ? "15" : paginationFilters.get("size"));
        String sortField = paginationFilters.get("sortField") == null ? "id" : paginationFilters.get("sortField");
        int sortOrder = Integer
                .valueOf(paginationFilters.get("sortOrder") == null ? "1" : paginationFilters.get("sortOrder"));

        pageList = (sortOrder != 1) ? PageRequest.of(page, pageSize, Sort.by(sortField).descending())
                : PageRequest.of(page, pageSize, Sort.by(sortField).ascending());

        if (processFilters.size() != 0) {
            FilterBuilder<Process> processFilterBuilder = new FilterBuilder<>(processFilters);

            String processIdListFiltered = processRepository.findAll(processFilterBuilder.build()).stream()
                    .map(p -> Long.toString(p.getId())).collect(Collectors.joining(","));

            processIdListFiltered = processIdListFiltered == "" ? "0" : processIdListFiltered;

            submissionFilters.put("process", processIdListFiltered);
        }

        FilterBuilder<Submission> submissionFilterBuilder = new FilterBuilder<>(submissionFilters);
        return submissionRepository.findAll(submissionFilterBuilder.build(), pageList);
    }

    @GetMapping("/{submissionId}/steps")
    public List<SubmissionStep> indexSteps(@PathVariable(value = "submissionId") Long submissionId) {
        return submissionStepRepository.findBySubmissionIdOrderByIdAsc(submissionId);
    }

    @PostMapping("/{submissionId}/acknowledgement")
    public Submission setAcknowledgementFlag(@PathVariable(value = "submissionId") Long submissionId,
            @RequestBody AcknowledgeDTO acknowledgeDTO, Authentication authentication) {
        // Validate user is the app owner or member of team
        Long sso = (Long) authentication.getPrincipal();
        Submission submission = submissionRepository.findById(submissionId).get();

        if (isProcessUser(sso, submission.getProcess().getId())
                || AcknowledgeLib.IsAllowedToAcknowledge(submission.getProcess(), sso)) {
            updateSubmissionAcknowledgementDetails(submission, acknowledgeDTO.getAcknowledgementNote());
            return submissionRepository.save(submission);
        } else {
            throw new RuntimeException("Only Application Owner and team can acknowledge a failure.");
        }
    }

    private void updateSubmissionAcknowledgementDetails(Submission submission, String notes) {
        submission.setAcknowledgementFlag(true);
        submission.setAcknowledgementDate(new Date());
        submission.setAcknowledgementNote(notes);
    }

    @PostMapping("/{submissionId}/manual-close")
    public Submission manualClose(@PathVariable(value = "submissionId") Long submissionId,
            @RequestBody ManualSubmissionStepRequest manualSubmissionStepRequest, Authentication authentication) {

        Long sso = (Long) authentication.getPrincipal();

        String token = JWT.create().withSubject(Long.toString(manualSubmissionStepRequest.getProcessId()))
                .sign(HMAC512(app_secret.getBytes()));

        if (isProcessUser(sso, manualSubmissionStepRequest.getProcessId())) {
            // Assign notes and status
            try {
                ManualTouchpoint.DoManualTouchpoint(token, submissionId, manualSubmissionStepRequest.getStatus(),
                        manualSubmissionStepRequest.getNotes(), manualSubmissionStepRequest.getAppsApiURL());
                Submission submission = submissionRepository.findById(submissionId).orElseThrow(NotFoundException::new);
                if (manualSubmissionStepRequest.getStatus().equalsIgnoreCase("failed")) {
                    updateSubmissionAcknowledgementDetails(submission, manualSubmissionStepRequest.getNotes());
                    submission = submissionRepository.save(submission);
                }
                // Send email
                User appOwner = submission.getProcess().getAppOwner();
                if (appOwner != null) {
                    this.spotlightEmailService.genericSend(NotificationTemplate.MANUAL_CLOSE, appOwner.getEmail(), null,
                            submission, null, null, null, null, null);
                }
                return submission;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                throw new RuntimeException("Unknown error. Get help from Spotlight support team!");
            }
        } else {
            throw new RuntimeException("Only Application Owner and team can manually close a submission.");
        }
    }

    @GetMapping("/submission-count")
    List<?> getSubmissionStatusCount(@RequestParam Map<String, String> filters, Authentication authentication) {
        Integer days = Integer.parseInt(filters.get("days"));
        int[] processList = Arrays.stream(filters.get("childId").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] parentList = Arrays.stream(filters.get("parentId").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] senderList = Arrays.stream(filters.get("sender").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] receiverList = Arrays.stream(filters.get("receiver").split(",")).mapToInt(Integer::parseInt).toArray();
        String bu = filters.get("bu");
        String adHoc = filters.get("adHoc");
        return submissionRepository.findSubmissionStatusCount(days, processList, parentList, senderList, receiverList,
                bu, adHoc);
    }

    @GetMapping("/submission-count/{status}")
    List<?> getSubmissionStatusCountByProcess(@RequestParam Map<String, String> filters,
            @PathVariable(value = "status") String status, Authentication authentication) {
        Integer statusId = 0;
        switch (status.toLowerCase()) {
        case "failed":
            statusId = 4;
            break;
        case "warning":
            statusId = 3;
            break;
        case "long":
            statusId = 997;
            break;
        case "delayed":
            statusId = 998;
            break;
        default:
            throw new RuntimeException("Status doesn't exists.");
        }

        Integer days = Integer.parseInt(filters.get("days"));
        int[] processList = Arrays.stream(filters.get("childId").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] parentList = Arrays.stream(filters.get("parentId").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] senderList = Arrays.stream(filters.get("sender").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] receiverList = Arrays.stream(filters.get("receiver").split(",")).mapToInt(Integer::parseInt).toArray();
        String bu = filters.get("bu");
        String adHoc = filters.get("adHoc");

        // Different query depending if from t_submission or t_notification_log
        if (statusId == 4 || statusId == 3) {
            return submissionRepository.findSubmissionStatusPerProcessByDate(statusId, days, processList, parentList,
                    senderList, receiverList, bu, adHoc);
        } else {
            return submissionRepository.findSubmissionStatusPerProcessByDate2(statusId, days, processList, parentList,
                    senderList, receiverList, bu, adHoc);
        }

    }

    @GetMapping("/submission-drill-in-progress")
    List<?> getSubmissionInProgresList(@RequestParam Map<String, String> filters, Authentication authentication) {

        Integer days = Integer.parseInt(filters.get("days") != null ? filters.get("days") : "7");
        int[] processList = Arrays.stream(filters.get("childId").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] parentList = Arrays.stream(filters.get("parentId").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] senderList = Arrays.stream(filters.get("sender").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] receiverList = Arrays.stream(filters.get("receiver").split(",")).mapToInt(Integer::parseInt).toArray();
        String bu = filters.get("bu");
        String adHoc = filters.get("adHoc");

        return submissionRepository.findSubmissionsInProgress(days, processList, parentList, senderList, receiverList,
                bu, adHoc);
    }

    @GetMapping("/submission-drill-failed")
    List<?> getSubmissionFailedList(@RequestParam Map<String, String> filters, Authentication authentication) {

        Integer days = Integer.parseInt(filters.get("days") != null ? filters.get("days") : "7");
        int[] processList = Arrays.stream(filters.get("childId").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] parentList = Arrays.stream(filters.get("parentId").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] senderList = Arrays.stream(filters.get("sender").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] receiverList = Arrays.stream(filters.get("receiver").split(",")).mapToInt(Integer::parseInt).toArray();
        String bu = filters.get("bu");
        String adHoc = filters.get("adHoc");
        return submissionRepository.findSubmissionsFailed(days, processList, parentList, senderList, receiverList, bu,
                adHoc);
    }

    @GetMapping("/submission-drill-delayed")
    List<?> getSubmissionDelayedList(@RequestParam Map<String, String> filters, Authentication authentication) {
        Integer days = Integer.parseInt(filters.get("days") != null ? filters.get("days") : "7");
        int[] processList = Arrays.stream(filters.get("childId").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] parentList = Arrays.stream(filters.get("parentId").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] senderList = Arrays.stream(filters.get("sender").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] receiverList = Arrays.stream(filters.get("receiver").split(",")).mapToInt(Integer::parseInt).toArray();
        // String bu = filters.get("bu");
        // String adHoc = filters.get("adHoc");

        return submissionRepository.findSubmissionsDelayed(days, processList, parentList, senderList, receiverList);
    }

    @GetMapping("/submission-drill-warning")
    List<?> getSubmissionWarningList(@RequestParam Map<String, String> filters, Authentication authentication) {
        Integer days = Integer.parseInt(filters.get("days") != null ? filters.get("days") : "7");
        int[] processList = Arrays.stream(filters.get("childId").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] parentList = Arrays.stream(filters.get("parentId").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] senderList = Arrays.stream(filters.get("sender").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] receiverList = Arrays.stream(filters.get("receiver").split(",")).mapToInt(Integer::parseInt).toArray();
        String bu = filters.get("bu");
        String adHoc = filters.get("adHoc");

        return submissionRepository.findSubmissionsWarning(days, processList, parentList, senderList, receiverList, bu,
                adHoc);
    }

    @GetMapping("/submission-drill-success")
    List<?> getSubmissionSuccessList(@RequestParam Map<String, String> filters, Authentication authentication) {
        Integer days = Integer.parseInt(filters.get("days") != null ? filters.get("days") : "7");
        int[] processList = Arrays.stream(filters.get("childId").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] parentList = Arrays.stream(filters.get("parentId").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] senderList = Arrays.stream(filters.get("sender").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] receiverList = Arrays.stream(filters.get("receiver").split(",")).mapToInt(Integer::parseInt).toArray();
        String bu = filters.get("bu");
        String adHoc = filters.get("adHoc");

        return submissionRepository.findSubmissionsSuccess(days, processList, parentList, senderList, receiverList, bu,
                adHoc);
    }

}
