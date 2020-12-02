package com.ge.finance.spotlight.controllers;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import com.ge.finance.spotlight.dto.ScheduleSummaryDTO;
import com.ge.finance.spotlight.exceptions.ForbiddenException;
import com.ge.finance.spotlight.exceptions.NotFoundException;
import com.ge.finance.spotlight.models.ProcessUser;
import com.ge.finance.spotlight.models.ScheduleReport;
import com.ge.finance.spotlight.models.User;
import com.ge.finance.spotlight.repositories.AnalyticsReportRepository;
import com.ge.finance.spotlight.repositories.ProcessUserRepository;
import com.ge.finance.spotlight.repositories.ScheduleReportRepository;
import com.ge.finance.spotlight.repositories.UserRepository;
import com.ge.finance.spotlight.services.ScheduleReportService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/summaryreport")
public class ScheduleReportController {
    
    private UserRepository userRepository;
    private ProcessUserRepository processUserRepository;
    private ScheduleReportRepository scheduleReportRepository;
    private ScheduleReportService scheduleReportService;
    private AnalyticsReportRepository analyticsReportRepository;             

    public ScheduleReportController(UserRepository userRepository, ProcessUserRepository processUserRepository,
             ScheduleReportRepository scheduleReportRepository,
            ScheduleReportService scheduleReportService, AnalyticsReportRepository analyticsReportRepository) {

        this.userRepository = userRepository;
        this.processUserRepository = processUserRepository;        
        this.scheduleReportRepository = scheduleReportRepository;
        this.scheduleReportService = scheduleReportService;
        this.analyticsReportRepository = analyticsReportRepository;                    
    }

    @PostMapping("/{processId}/schedulesummaryreport")
    ScheduleReport createScheduleSummaryReport(@PathVariable(name = "processId") Long processId,
            @RequestBody ScheduleSummaryDTO scheduleSummaryDTO, Authentication authentication) {

        Long sso = (Long) authentication.getPrincipal();
        User user = userRepository.findFirstBySso(sso);

        Calendar scheduleStart = Calendar.getInstance();
        scheduleStart.setTime(scheduleSummaryDTO.getStartDate());
        scheduleStart.setTime(scheduleSummaryDTO.getStartDate());
        scheduleStart.set(Calendar.HOUR_OF_DAY, 0);
        scheduleStart.set(Calendar.MINUTE, 0);
        scheduleStart.set(Calendar.SECOND, 0);
        scheduleStart.set(Calendar.MILLISECOND, 0);

        Calendar scheduleEnd = Calendar.getInstance();
        scheduleEnd.setTime(scheduleSummaryDTO.getEndDate());
        scheduleEnd.set(Calendar.HOUR_OF_DAY, 0);
        scheduleEnd.set(Calendar.MINUTE, 0);
        scheduleEnd.set(Calendar.SECOND, 0);
        scheduleEnd.set(Calendar.MILLISECOND, 0);

        if (isProcessUser(sso, processId) || isAdmin(authentication.getAuthorities())) {
            // scheduleReport.setProcessId(processRepository.findById(processId).orElseThrow(NotFoundException::new));
            ScheduleReport scheduleReport = new ScheduleReport();
            scheduleReport.setRangeLength(scheduleSummaryDTO.getRangeLength());
            scheduleReport.setStartDate(scheduleStart.getTime());
            scheduleReport.setEndDate(scheduleEnd.getTime());
            scheduleReport.setAdditionalEmails(scheduleSummaryDTO.getAdditionalEmails());
            scheduleReport.setProcessId(scheduleSummaryDTO.getProcessId());
            scheduleReport.setRecurrencePattern(scheduleSummaryDTO.getRecurrencePattern());
            scheduleReport.setUser(user);
            scheduleReport.setSettings(scheduleSummaryDTO.getSettings());

            scheduleReport = scheduleReportRepository.save(scheduleReport);
            scheduleSummaryDTO.setId(scheduleReport.getId());
            scheduleSummaryDTO.setStartDate(scheduleReport.getStartDate());
            scheduleReportService.setupSummaryReport(scheduleSummaryDTO);

            return scheduleReport;
        } else {
            throw new ForbiddenException();
        }
    }

    @GetMapping("/list")
    List<ScheduleReport> getScheduledReports(Authentication authentication) {
        if (isAdmin(authentication.getAuthorities())) {
            return scheduleReportRepository.findAll();
        } else {
            Long sso = (Long) authentication.getPrincipal();
            User user = userRepository.findFirstBySso(sso);

            List<Long> scheduleReportList = scheduleReportRepository.findByUserId(user.getId()).stream()
                    .map(ScheduleReport::getId).collect(Collectors.toList());
            if (!scheduleReportList.isEmpty()) {
                return scheduleReportRepository.findByUserId(user.getId());
            } else {
                throw new RuntimeException("No Scheduled Report.");
            }
        }
    }

    @DeleteMapping("/{reportId}/analyticsReport/{processId}")
    @Transactional
    public ScheduleReport deleteScheduleReport(@PathVariable(name = "reportId") Long reportId,
            @PathVariable(name = "processId") Long processId, Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        if (isProcessUser(sso, processId) || isAdmin(authentication.getAuthorities())) {
            ScheduleReport scheduleReport = scheduleReportRepository.findById(reportId)
                    .orElseThrow(NotFoundException::new);
            analyticsReportRepository.deleteByscheduleReportId(reportId);
            scheduleReportRepository.delete(scheduleReport);
            return scheduleReport;
        } else {
            throw new ForbiddenException();
        }
    }

    private boolean isProcessUser(Long sso, Long processId) {
        User user = userRepository.findFirstBySso(sso);
        List<ProcessUser> processUsers = processUserRepository.findByUserIdAndProcessId(user.getId(), processId);
        return !processUsers.isEmpty();
    }

    private boolean isAdmin(Collection<? extends GrantedAuthority> grantedAuthorities) {
        for (GrantedAuthority grantedAuthority : grantedAuthorities) {
            if (grantedAuthority.getAuthority().equals("admin")) {
                return true;
            }
        }
        return false;
    }

}