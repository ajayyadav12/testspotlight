package com.ge.finance.spotlight.controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ge.finance.spotlight.dto.AcknowledgeDTO;
import com.ge.finance.spotlight.exceptions.BadRequestException;
import com.ge.finance.spotlight.filters.FilterBuilder;
import com.ge.finance.spotlight.libs.AcknowledgeLib;
import com.ge.finance.spotlight.models.Process;
import com.ge.finance.spotlight.models.ProcessUser;
import com.ge.finance.spotlight.models.ScheduledSubmission;
import com.ge.finance.spotlight.models.User;
import com.ge.finance.spotlight.repositories.ProcessRepository;
import com.ge.finance.spotlight.repositories.ProcessUserRepository;
import com.ge.finance.spotlight.repositories.ScheduledSubmissionRepository;
import com.ge.finance.spotlight.repositories.UserRepository;

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

    private UserRepository userRepository;
    private ProcessUserRepository processUserRepository;
    private ProcessRepository processRepository;

    private ScheduledSubmissionController(ScheduledSubmissionRepository scheduledSubmissionRepository,
            UserRepository userRepository, ProcessUserRepository processUserRepository,
            ProcessRepository processRepository) {
        this.scheduledSubmissionRepository = scheduledSubmissionRepository;
        this.userRepository = userRepository;
        this.processUserRepository = processUserRepository;
        this.processRepository = processRepository;
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

        return scheduledSubmissionRepository.findByStartTimeIsBetweenAndSubmissionIdIsNullOrderByStartTimeAsc(fromDate,
                toDate);
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

        List<ScheduledSubmission> scheList = scheduledSubmissionRepository
                .findByStartTimeIsBetweenAndProcessIdIsInAndSubmissionIdIsNullOrderByStartTimeAsc(fromDate, toDate,
                        processIds);
        return scheduledSubmissionRepository
                .findByStartTimeIsBetweenAndProcessIdIsInAndSubmissionIdIsNullOrderByStartTimeAsc(fromDate, toDate,
                        processIds);
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

            return scheduledSubmissionRepository.findByStartTimeIsBetweenAndScheduleDefinitionIdIsInOrderByStartTimeAsc(
                    fromDate, toDate, scheduleDefId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException();
        }
    }

    @PostMapping("/{scheduleSubmissionId}/update")
    public ScheduledSubmission update(@PathVariable(value = "scheduleSubmissionId") Long scheduleSubmissionId,
            @RequestParam Date from, @RequestParam Date to, Authentication authentication) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(from);
            from = calendar.getTime();
            calendar.setTime(to);
            to = calendar.getTime();
            ScheduledSubmission submission = scheduledSubmissionRepository.findById(scheduleSubmissionId).get();
            submission.setStartTime(from);
            submission.setEndTime(to);
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

}
