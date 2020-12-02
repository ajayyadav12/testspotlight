package com.ge.finance.spotlight.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.ge.finance.spotlight.exceptions.NotFoundException;
import com.ge.finance.spotlight.filters.FilterBuilder;
import com.ge.finance.spotlight.models.ParentSubmission;
import com.ge.finance.spotlight.models.Process;
import com.ge.finance.spotlight.models.StatusReport;
import com.ge.finance.spotlight.repositories.ParentSubmissionRepository;
import com.ge.finance.spotlight.repositories.ProcessRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/parentSubmissions")
public class ParentSubmissionController {

        private ProcessRepository processRepository;
        private ParentSubmissionRepository parentSubmissionRepository;

        private List<String> allowedProcessFilters = Arrays.asList("sender", "receiver", "processType", "parentId");
        private List<String> allowedSubmissionFilters = Arrays.asList("from", "to", "status", "adHoc");
        private List<String> allowedParentSubmissionFilters = Arrays.asList("processId", "from", "to");

        public ParentSubmissionController(ProcessRepository processRepository,
                        ParentSubmissionRepository parentSubmissionRepository) {
                this.processRepository = processRepository;
                this.parentSubmissionRepository = parentSubmissionRepository;
        }

        @GetMapping("/")
        public Page<ParentSubmission> index(@RequestParam Map<String, String> filters, Authentication authentication) {
                Pageable pageList;
                Map<String, String> processFilters = filters.entrySet().stream()
                                .filter(entry -> allowedProcessFilters.contains(entry.getKey()))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                Map<String, String> submissionFilters = filters.entrySet().stream()
                                .filter(entry -> allowedSubmissionFilters.contains(entry.getKey()))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                FilterBuilder<Process> processFilterBuilder = new FilterBuilder<>(processFilters);
                String processIdListFiltered = processRepository.findAll(processFilterBuilder.build()).stream()
                                .map(p -> Long.toString(p.getId())).collect(Collectors.joining(","));
                if (processIdListFiltered == "") {
                        processIdListFiltered = "0";
                }

                int page = Integer.valueOf(filters.get("page") == null ? "0" : filters.get("page"));
                int pageSize = Integer.valueOf(filters.get("size") == null ? "10" : filters.get("size"));
                String sortField = filters.get("sortField") == null ? "id" : filters.get("sortField");
                int sortOrder = Integer
                        .valueOf(filters.get("sortOrder") == null ? "1" : filters.get("sortOrder"));

                if (sortOrder != 1) {
                        pageList = PageRequest.of(page, pageSize, Sort.by(sortField).descending());
                } else {
                        pageList = PageRequest.of(page, pageSize, Sort.by(sortField).ascending());
                }

                submissionFilters.put("process", processIdListFiltered);
                FilterBuilder<ParentSubmission> submissionFilterBuilder = new FilterBuilder<>(submissionFilters);
                return parentSubmissionRepository.findAll(submissionFilterBuilder.build(), pageList);
        }

        @GetMapping("/{parentId}")
        ParentSubmission get(@PathVariable(value = "parentId") Long parentId) {
                return parentSubmissionRepository.findById(parentId).orElseThrow(NotFoundException::new);
        }

        @GetMapping("/list")
        public List<StatusReport> parentSubmissions(@RequestParam Map<String, String> filters,
                        Authentication authentication) {
                Long processId = null;
                Calendar fromDate = Calendar.getInstance();
                Calendar toDate = Calendar.getInstance();
                Date from = null;
                Date to = null;
                Map<String, String> submissionFilters = filters.entrySet().stream()
                                .filter(entry -> allowedParentSubmissionFilters.contains(entry.getKey()))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                try {
                        from = new SimpleDateFormat("yyyy-MM-dd").parse(
                                        submissionFilters.get("from") == null ? "" : submissionFilters.get("from"));
                        fromDate.setTime(from);
                        fromDate.add(Calendar.DATE, 1);
                        from = fromDate.getTime();
                } catch (ParseException e) {
                        Calendar c = new GregorianCalendar();
                        c.add(Calendar.DATE, -30);
                        from = c.getTime();
                }
                try {
                        to = new SimpleDateFormat("yyyy-MM-dd")
                                        .parse(submissionFilters.get("to") == null ? "" : submissionFilters.get("to"));
                        toDate.setTime(to);
                        toDate.add(Calendar.DATE, 1);
                        to = toDate.getTime();
                } catch (ParseException e) {
                        Calendar c = new GregorianCalendar();
                        // to=c.getTime();
                        to = new Date();
                }
                processId = Long.valueOf(
                                submissionFilters.get("processId") == null ? "0" : submissionFilters.get("processId"));

                List<ParentSubmission> submissions = parentSubmissionRepository
                                .findByProcessIdAndStartTimeIsBetweenOrderByStartTimeAsc(processId, from, to);
                List<StatusReport> recordsList = new ArrayList<StatusReport>();

                Iterator<ParentSubmission> it = submissions.iterator();

                while (it.hasNext()) {
                        ParentSubmission submission = it.next();

                        StatusReport recordReport = new StatusReport();

                        if (submission.getStartTime() != null && submission.getEndTime() != null) {
                                recordReport.setDuration(getDateDiff(submission.getStartTime(), submission.getEndTime(),
                                                TimeUnit.SECONDS));
                                recordReport.setSubmission(submission);
                                recordsList.add(recordReport);
                        }
                }

                return recordsList;
        }

        /**
         * Get a diff between two dates
         * 
         * @param date1    the oldest date
         * @param date2    the newest date
         * @param timeUnit the unit in which you want the diff
         * @return the diff value, in the provided unit
         */
        public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
                long diffInMillies = date2.getTime() - date1.getTime();
                return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
        }

}
