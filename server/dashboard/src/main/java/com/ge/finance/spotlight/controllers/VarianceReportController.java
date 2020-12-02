package com.ge.finance.spotlight.controllers;

import com.ge.finance.spotlight.models.Submission;
import com.ge.finance.spotlight.repositories.SubmissionRepository;
import com.ge.finance.spotlight.responses.VarianceReport;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

@RestController
@RequestMapping("/v1/varianceReport")
public class VarianceReportController {

    private SubmissionRepository submissionRepository;

    private List<String> allowedSubmissionFilters = Arrays.asList("processId", "from", "to");

    public VarianceReportController(SubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

    @GetMapping("/")
    public List<VarianceReport> index(@RequestParam Map<String, String> filters, Authentication authentication) {
        Long processId = null;
        Calendar fromDate = Calendar.getInstance();
        Calendar toDate = Calendar.getInstance();
        Date from = null;
        Date to = null;
        Map<String, String> submissionFilters = filters.entrySet().stream()
                .filter(entry -> allowedSubmissionFilters.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        try {
            from = new SimpleDateFormat("yyyy-MM-dd")
                    .parse(submissionFilters.get("from") == null ? "" : submissionFilters.get("from"));
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
        processId = Long.valueOf(submissionFilters.get("processId") == null ? "0" : submissionFilters.get("processId"));

        List<Submission> submissions = submissionRepository
                .findByProcessIdAndStartTimeIsBetweenOrderByStartTimeAsc(processId, from, to);
        List<VarianceReport> recordsList = new ArrayList<VarianceReport>();

        Iterator<Submission> it = submissions.iterator();

        while (it.hasNext()) {
            Submission submission = it.next();

            VarianceReport recordReport = new VarianceReport();

            if (submission.getStartTime() != null && submission.getEndTime() != null) {
                recordReport
                        .setDuration(getDateDiff(submission.getStartTime(), submission.getEndTime(), TimeUnit.SECONDS));
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
