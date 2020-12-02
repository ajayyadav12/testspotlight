package com.ge.finance.spotlight.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ge.finance.spotlight.dto.ScheduleSummaryDTO;
import com.ge.finance.spotlight.exceptions.NotFoundException;
import com.ge.finance.spotlight.models.AnalyticsReport;
import com.ge.finance.spotlight.repositories.AnalyticsReportRepository;
import com.ge.finance.spotlight.repositories.ProcessRepository;

import org.springframework.stereotype.Service;

@Service
public class ScheduleReportServiceImpl implements ScheduleReportService {

    static class Custom {

        public String startTime;
        public String endTime;
        public String submission_level;
        public Long distributionList;
        public Long notification;
    }

    static class Monthly {

        public String startTime;
        public String endTime;
        public String submissionLevel;
        public Long distributionList;
        public Long notification;
        public String option;
        public int daysNumber;
        public int monthsNumber;
        public int ocurrence;
        public String day;
        public int every;
        public int fromLCD;
        public int toLCD;
        public int monthsLCD;
    }

    static class Weekly {
        public int recurEvery;
        public Boolean[] days;
    }

    private void setDayName(String day, Calendar calendar) {
        switch (day) {
        case "Monday":
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            break;
        case "Tuesday":
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
            break;
        case "Wednesday":
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
            break;
        case "Thursday":
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
            break;
        case "Friday":
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
            break;
        case "Saturday":
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
            break;
        case "Sunday":
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            break;
        }
    }
    
    private AnalyticsReportRepository analyticsReportRepository;    
    private ProcessRepository processRepository;    

    public ScheduleReportServiceImpl(
            AnalyticsReportRepository analyticsReportRepository, ProcessRepository processRepository) {        
        this.analyticsReportRepository = analyticsReportRepository;        
        this.processRepository = processRepository;
    }

    public void setupSummaryReport(ScheduleSummaryDTO scheduleReport) {

        List<AnalyticsReport> analyticsReports = new ArrayList<>();

        switch (scheduleReport.getRecurrencePattern()) {

        case 'M':
            try {
                Calendar scheduleStart = null;
                Calendar scheduleEnd = null;
                Date startDate = null;
                Date endDate = null;
                Calendar schedule = null;
                Date afterEndDate = null;
                Calendar scheduleAfterDate = null;
                Monthly monthly = new ObjectMapper().readValue(scheduleReport.getSettings(), Monthly.class);
                switch (monthly.option) {
                case "day":

                    scheduleStart = Calendar.getInstance();
                    scheduleStart.setTime(scheduleReport.getStartDate());
                    scheduleStart.set(Calendar.HOUR_OF_DAY, 0);
                    scheduleStart.set(Calendar.MINUTE, 0);
                    scheduleStart.set(Calendar.SECOND, 0);
                    scheduleStart.set(Calendar.MILLISECOND, 0);

                    scheduleEnd = Calendar.getInstance();
                    scheduleEnd.setTime(scheduleReport.getEndDate());
                    scheduleEnd.set(Calendar.HOUR_OF_DAY, 0);
                    scheduleEnd.set(Calendar.MINUTE, 0);
                    scheduleEnd.set(Calendar.SECOND, 0);
                    scheduleEnd.set(Calendar.MILLISECOND, 0);

                    startDate = scheduleStart.getTime();
                    endDate = scheduleEnd.getTime();

                    schedule = Calendar.getInstance();
                    schedule.setTime(startDate);
                    schedule.set(Calendar.DATE, monthly.daysNumber);
                    schedule.add(Calendar.MINUTE, 30);
                    scheduleAfterDate = Calendar.getInstance();
                    scheduleAfterDate.setTime(scheduleReport.getEndDate());
                    scheduleAfterDate.add(Calendar.DATE, 1);
                    scheduleAfterDate.set(Calendar.HOUR_OF_DAY, 0);
                    scheduleAfterDate.set(Calendar.MINUTE, 0);
                    scheduleAfterDate.set(Calendar.SECOND, 0);
                    scheduleAfterDate.set(Calendar.MILLISECOND, 0);
                    afterEndDate = scheduleAfterDate.getTime();

                    if (schedule.getTime().before(startDate)) {
                        schedule.add(Calendar.MONTH, monthly.monthsNumber);
                        // schedule.set(Calendar.DATE, monthly.daysNumber);
                        // schedule.add(Calendar.MINUTE, 30);
                    }

                    while (schedule.getTime().after(startDate) && schedule.getTime().before(afterEndDate)) {

                        scheduleStart.set(Calendar.YEAR, schedule.get(Calendar.YEAR));
                        scheduleStart.set(Calendar.MONTH, schedule.get(Calendar.MONTH));
                        scheduleStart.set(Calendar.DAY_OF_MONTH, schedule.get(Calendar.DAY_OF_MONTH));

                        scheduleEnd.set(Calendar.YEAR, schedule.get(Calendar.YEAR));
                        scheduleEnd.set(Calendar.MONTH, schedule.get(Calendar.MONTH));
                        scheduleEnd.set(Calendar.DAY_OF_MONTH, schedule.get(Calendar.DAY_OF_MONTH));

                        AnalyticsReport analyticsReport = new AnalyticsReport();
                        analyticsReport.setScheduleReportId(scheduleReport.getId());
                        analyticsReport.setProcess(processRepository.findById(scheduleReport.getProcessId())
                                .orElseThrow(NotFoundException::new));
                        analyticsReport.setStartTime(scheduleStart.getTime());
                        analyticsReport.setEndTime(scheduleEnd.getTime());
                        analyticsReport.setSubmissionLevel(scheduleReport.getSubmissionLevel());
                        analyticsReports.add(analyticsReport);

                        schedule.set(Calendar.DATE, monthly.daysNumber);
                        schedule.add(Calendar.MONTH, monthly.monthsNumber);
                    }

                    break;

                case "the":

                    scheduleStart = Calendar.getInstance();
                    scheduleStart.setTime(scheduleReport.getStartDate());
                    scheduleStart.set(Calendar.HOUR_OF_DAY, 0);
                    scheduleStart.set(Calendar.MINUTE, 0);
                    scheduleStart.set(Calendar.SECOND, 0);
                    scheduleStart.set(Calendar.MILLISECOND, 0);

                    scheduleEnd = Calendar.getInstance();
                    scheduleEnd.setTime(scheduleReport.getEndDate());
                    scheduleEnd.set(Calendar.HOUR_OF_DAY, 0);
                    scheduleEnd.set(Calendar.MINUTE, 0);
                    scheduleEnd.set(Calendar.SECOND, 0);
                    scheduleEnd.set(Calendar.MILLISECOND, 0);

                    startDate = scheduleStart.getTime();
                    endDate = scheduleEnd.getTime();

                    schedule = Calendar.getInstance();
                    schedule.setTime(startDate);
                    // schedule.add(Calendar.MONTH, monthly.every);
                    schedule.add(Calendar.MINUTE, 30);

                    scheduleAfterDate = Calendar.getInstance();
                    scheduleAfterDate.setTime(endDate);
                    scheduleAfterDate.add(Calendar.DATE, 1);
                    scheduleAfterDate.set(Calendar.HOUR_OF_DAY, 0);
                    scheduleAfterDate.set(Calendar.MINUTE, 0);
                    scheduleAfterDate.set(Calendar.SECOND, 0);
                    scheduleAfterDate.set(Calendar.MILLISECOND, 0);
                    afterEndDate = scheduleAfterDate.getTime();
                    schedule.set(Calendar.DAY_OF_WEEK_IN_MONTH, monthly.ocurrence);
                    setDayName(monthly.day, schedule);

                    if (schedule.getTime().before(startDate)) {
                        schedule.add(Calendar.MONTH, monthly.every);
                        schedule.set(Calendar.DAY_OF_WEEK_IN_MONTH, monthly.ocurrence);
                        setDayName(monthly.day, schedule);
                    }
                    while (schedule.getTime().after(startDate) && schedule.getTime().before(afterEndDate)) {

                        scheduleStart.set(Calendar.YEAR, schedule.get(Calendar.YEAR));
                        scheduleStart.set(Calendar.MONTH, schedule.get(Calendar.MONTH));
                        scheduleStart.set(Calendar.DAY_OF_MONTH, schedule.get(Calendar.DAY_OF_MONTH));

                        scheduleEnd.set(Calendar.YEAR, schedule.get(Calendar.YEAR));
                        scheduleEnd.set(Calendar.MONTH, schedule.get(Calendar.MONTH));
                        scheduleEnd.set(Calendar.DAY_OF_MONTH, schedule.get(Calendar.DAY_OF_MONTH));

                        AnalyticsReport analyticsReport = new AnalyticsReport();
                        analyticsReport.setScheduleReportId(scheduleReport.getId());
                        analyticsReport.setProcess(processRepository.findById(scheduleReport.getProcessId())
                                .orElseThrow(NotFoundException::new));
                        analyticsReport.setStartTime(scheduleStart.getTime());
                        analyticsReport.setEndTime(scheduleEnd.getTime());
                        analyticsReport.setSubmissionLevel(scheduleReport.getSubmissionLevel());
                        analyticsReports.add(analyticsReport);

                        schedule.add(Calendar.MONTH, monthly.every);

                        schedule.set(Calendar.DAY_OF_WEEK_IN_MONTH, monthly.ocurrence);
                        setDayName(monthly.day, schedule);
                    }
                    break;

                case "lcd":

                    scheduleStart = Calendar.getInstance();
                    scheduleStart.setTime(scheduleReport.getStartDate());
                    scheduleStart.set(Calendar.HOUR_OF_DAY, 0);
                    scheduleStart.set(Calendar.MINUTE, 0);
                    scheduleStart.set(Calendar.SECOND, 0);
                    scheduleStart.set(Calendar.MILLISECOND, 0);

                    scheduleEnd = Calendar.getInstance();
                    scheduleEnd.setTime(scheduleReport.getEndDate());
                    scheduleEnd.set(Calendar.HOUR_OF_DAY, 0);
                    scheduleEnd.set(Calendar.MINUTE, 0);
                    scheduleEnd.set(Calendar.SECOND, 0);
                    scheduleEnd.set(Calendar.MILLISECOND, 0);

                    startDate = scheduleStart.getTime();
                    endDate = scheduleEnd.getTime();

                    int intCounter = monthly.monthsLCD;
                    schedule = Calendar.getInstance();
                    schedule.setTime(startDate);
                    schedule.add(Calendar.MINUTE, 30);
                    scheduleAfterDate = Calendar.getInstance();
                    scheduleAfterDate.setTime(endDate);
                    scheduleAfterDate.add(Calendar.DATE, 1);
                    scheduleAfterDate.set(Calendar.HOUR_OF_DAY, 0);
                    scheduleAfterDate.set(Calendar.MINUTE, 0);
                    scheduleAfterDate.set(Calendar.SECOND, 0);
                    scheduleAfterDate.set(Calendar.MILLISECOND, 0);
                    afterEndDate = scheduleAfterDate.getTime();
                    // schedule.add(Calendar.MONTH, monthly.monthsLCD);
                    schedule.set(Calendar.DATE, schedule.getActualMaximum(Calendar.DATE));
                    int monthFlag = 0;
                    while (schedule.getTime().after(startDate) && schedule.getTime().before(afterEndDate)) {
                        for (int lcdCounter = monthly.fromLCD; lcdCounter <= monthly.toLCD; lcdCounter++) {
                            schedule.add(Calendar.DATE, lcdCounter);

                            if (!(schedule.getTime().before(startDate) || schedule.getTime().after(afterEndDate))) {

                                scheduleStart.set(Calendar.YEAR, schedule.get(Calendar.YEAR));
                                scheduleStart.set(Calendar.MONTH, schedule.get(Calendar.MONTH));
                                scheduleStart.set(Calendar.DAY_OF_MONTH, schedule.get(Calendar.DAY_OF_MONTH));

                                scheduleEnd.set(Calendar.YEAR, schedule.get(Calendar.YEAR));
                                scheduleEnd.set(Calendar.MONTH, schedule.get(Calendar.MONTH));
                                scheduleEnd.set(Calendar.DAY_OF_MONTH, schedule.get(Calendar.DAY_OF_MONTH));

                                AnalyticsReport analyticsReport = new AnalyticsReport();
                                analyticsReport.setScheduleReportId(scheduleReport.getId());
                                analyticsReport.setProcess(processRepository.findById(scheduleReport.getProcessId())
                                        .orElseThrow(NotFoundException::new));
                                analyticsReport.setStartTime(scheduleStart.getTime());
                                analyticsReport.setEndTime(scheduleEnd.getTime());
                                analyticsReport.setSubmissionLevel(scheduleReport.getSubmissionLevel());
                                analyticsReports.add(analyticsReport);

                            }

                            schedule = Calendar.getInstance();
                            if (monthFlag > 0) {
                                schedule.add(Calendar.MONTH, monthly.monthsLCD);
                            }
                            schedule.set(Calendar.DATE, schedule.getActualMaximum(Calendar.DATE));
                        }
                        if (monthFlag > 0) {
                            monthly.monthsLCD = monthly.monthsLCD + intCounter;
                        } else {
                            monthly.monthsLCD = monthly.monthsLCD;
                        }
                        schedule = Calendar.getInstance();
                        schedule.add(Calendar.MONTH, monthly.monthsLCD);
                        schedule.set(Calendar.DATE, schedule.getActualMaximum(Calendar.DATE));
                        monthFlag++;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            analyticsReportRepository.saveAll(analyticsReports);
        }
    }
}
