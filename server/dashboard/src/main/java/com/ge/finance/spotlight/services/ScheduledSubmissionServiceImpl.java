package com.ge.finance.spotlight.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ge.finance.spotlight.models.ScheduleDefinition;
import com.ge.finance.spotlight.models.ScheduledSubmission;
import com.ge.finance.spotlight.repositories.ScheduledSubmissionRepository;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class ScheduledSubmissionServiceImpl implements ScheduledSubmissionService {

    static class Custom {

        public Date startTime;
        public Date endTime;
    }

    static class Monthly {
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

    private void setScheduleDateTime(Calendar schedule, Calendar scheduleStart, Calendar scheduleEnd,
            List<ScheduledSubmission> scheduledSubmissions, ScheduleDefinition scheduleDefinition) {
        scheduleStart.set(Calendar.YEAR, schedule.get(Calendar.YEAR));
        scheduleStart.set(Calendar.MONTH, schedule.get(Calendar.MONTH));
        scheduleStart.set(Calendar.DAY_OF_MONTH, schedule.get(Calendar.DAY_OF_MONTH));

        scheduleEnd.set(Calendar.YEAR, schedule.get(Calendar.YEAR));
        scheduleEnd.set(Calendar.MONTH, schedule.get(Calendar.MONTH));
        scheduleEnd.set(Calendar.DAY_OF_MONTH, schedule.get(Calendar.DAY_OF_MONTH));

        ScheduledSubmission scheduledSubmission = new ScheduledSubmission();
        scheduledSubmission.setScheduleDefinitionId(scheduleDefinition.getId());
        scheduledSubmission.setProcess(scheduleDefinition.getProcess());
        scheduledSubmission.setStartTime(scheduleStart.getTime());
        scheduledSubmission.setEndTime(scheduleEnd.getTime());
        scheduledSubmission.setTolerance(scheduleDefinition.getTolerance());
        scheduledSubmissions.add(scheduledSubmission);
    }

    private ScheduledSubmissionRepository scheduledSubmissionRepository;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

    public ScheduledSubmissionServiceImpl(ScheduledSubmissionRepository scheduledSubmissionRepository) {
        this.scheduledSubmissionRepository = scheduledSubmissionRepository;
    }

    @Override
    public void setupScheduledSubmissions(ScheduleDefinition scheduleDefinition) {
        List<ScheduledSubmission> scheduledSubmissions = new ArrayList<>();
        switch (scheduleDefinition.getRecurrencePattern()) {
        case 'C':
            try {
                String settings = scheduleDefinition.getSettings();

                String[] dateStrings = settings.substring(1, settings.length() - 1).replace("{", "#{")
                        .replace("},", "}").split("#");
                for (String timesRecord : dateStrings) {
                    if (timesRecord.isBlank())
                        continue;
                    Custom time = new ObjectMapper().readValue(timesRecord, Custom.class);                    
                    Calendar scheduleStart = Calendar.getInstance();
                    Calendar scheduleEnd = Calendar.getInstance();
                    Calendar schedule = Calendar.getInstance();
                    Calendar endSchedule = Calendar.getInstance();

                    schedule.setTime(time.startTime);
                    endSchedule.setTime(time.endTime);

                    scheduleStart.set(Calendar.YEAR, schedule.get(Calendar.YEAR));
                    scheduleStart.set(Calendar.MONTH, schedule.get(Calendar.MONTH));
                    scheduleStart.set(Calendar.DAY_OF_MONTH, schedule.get(Calendar.DAY_OF_MONTH));
                    scheduleStart.set(Calendar.HOUR_OF_DAY, schedule.get(Calendar.HOUR_OF_DAY));
                    scheduleStart.set(Calendar.MINUTE, schedule.get(Calendar.MINUTE));
                    scheduleStart.set(Calendar.SECOND, schedule.get(Calendar.SECOND));

                    scheduleEnd.set(Calendar.YEAR, endSchedule.get(Calendar.YEAR));
                    scheduleEnd.set(Calendar.MONTH, endSchedule.get(Calendar.MONTH));
                    scheduleEnd.set(Calendar.DAY_OF_MONTH, endSchedule.get(Calendar.DAY_OF_MONTH));
                    scheduleEnd.set(Calendar.HOUR_OF_DAY, endSchedule.get(Calendar.HOUR_OF_DAY));
                    scheduleEnd.set(Calendar.MINUTE, endSchedule.get(Calendar.MINUTE));
                    scheduleEnd.set(Calendar.SECOND, endSchedule.get(Calendar.SECOND));

                    // Create Scheduled Submission.
                    ScheduledSubmission scheduledSubmission = new ScheduledSubmission();
                    scheduledSubmission.setScheduleDefinitionId(scheduleDefinition.getId());
                    scheduledSubmission.setProcess(scheduleDefinition.getProcess());
                    scheduledSubmission.setStartTime(scheduleStart.getTime());
                    scheduledSubmission.setEndTime(scheduleEnd.getTime());
                    scheduledSubmission.setTolerance(scheduleDefinition.getTolerance());
                    scheduledSubmissions.add(scheduledSubmission);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            break;
        case 'D':
            try {

                Calendar scheduleStart = null;
                Calendar scheduleEnd = null;
                Date startDate = null;
                Date endDate = null;
                Calendar schedule = null;
                Date afterEndDate = null;
                Calendar scheduleAfterDate = null;
                Daily daily = new ObjectMapper().readValue(scheduleDefinition.getSettings(), Daily.class);
                switch (daily.option) {
                case "daily":
                    if (!daily.isEveryWeekday) {

                        scheduleStart = Calendar.getInstance();
                        scheduleStart.setTime(scheduleDefinition.getStartTime());

                        scheduleEnd = Calendar.getInstance();
                        scheduleEnd.setTime(scheduleDefinition.getEndTime());

                        startDate = scheduleDefinition.getScheduleStartDate();
                        endDate = scheduleDefinition.getScheduleEndDate();

                        schedule = Calendar.getInstance();
                        schedule.setTime(startDate);
                        // schedule.add(Calendar.DAY_OF_MONTH, daily.recurEvery);
                        scheduleAfterDate = Calendar.getInstance();
                        scheduleAfterDate.setTime(endDate);
                        scheduleAfterDate.add(Calendar.DAY_OF_MONTH, 1);
                        afterEndDate = scheduleAfterDate.getTime();

                        while ((schedule.getTime().after(startDate) || schedule.getTime().equals(startDate))
                                && schedule.getTime().before(afterEndDate)) {

                            scheduleStart.set(Calendar.YEAR, schedule.get(Calendar.YEAR));
                            scheduleStart.set(Calendar.MONTH, schedule.get(Calendar.MONTH));
                            scheduleStart.set(Calendar.DAY_OF_MONTH, schedule.get(Calendar.DAY_OF_MONTH));

                            scheduleEnd.set(Calendar.YEAR, schedule.get(Calendar.YEAR));
                            scheduleEnd.set(Calendar.MONTH, schedule.get(Calendar.MONTH));
                            scheduleEnd.set(Calendar.DAY_OF_MONTH, schedule.get(Calendar.DAY_OF_MONTH));

                            ScheduledSubmission scheduledSubmission = new ScheduledSubmission();
                            scheduledSubmission.setScheduleDefinitionId(scheduleDefinition.getId());
                            scheduledSubmission.setProcess(scheduleDefinition.getProcess());
                            scheduledSubmission.setStartTime(scheduleStart.getTime());
                            scheduledSubmission.setEndTime(scheduleEnd.getTime());
                            scheduledSubmission.setTolerance(scheduleDefinition.getTolerance());
                            scheduledSubmissions.add(scheduledSubmission);

                            schedule.add(Calendar.DAY_OF_MONTH, daily.recurEvery);
                            // schedule.add(Calendar.MONTH, daily.monthsNumber);
                        }
                    } else {

                        scheduleStart = Calendar.getInstance();
                        scheduleStart.setTime(scheduleDefinition.getStartTime());

                        scheduleEnd = Calendar.getInstance();
                        scheduleEnd.setTime(scheduleDefinition.getEndTime());

                        startDate = scheduleDefinition.getScheduleStartDate();
                        endDate = scheduleDefinition.getScheduleEndDate();

                        schedule = Calendar.getInstance();
                        schedule.setTime(startDate);
                        // schedule.add(Calendar.DAY_OF_MONTH, daily.recurEvery);
                        scheduleAfterDate = Calendar.getInstance();
                        scheduleAfterDate.setTime(endDate);
                        scheduleAfterDate.add(Calendar.DAY_OF_MONTH, 1);
                        afterEndDate = scheduleAfterDate.getTime();

                        while ((schedule.getTime().after(startDate) || schedule.getTime().equals(startDate))
                                && schedule.getTime().before(afterEndDate)) {

                            if (schedule.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
                                    || schedule.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {

                                schedule.add(Calendar.DAY_OF_MONTH, 1);
                            } else {

                                scheduleStart.set(Calendar.YEAR, schedule.get(Calendar.YEAR));
                                scheduleStart.set(Calendar.MONTH, schedule.get(Calendar.MONTH));
                                scheduleStart.set(Calendar.DAY_OF_MONTH, schedule.get(Calendar.DAY_OF_MONTH));

                                scheduleEnd.set(Calendar.YEAR, schedule.get(Calendar.YEAR));
                                scheduleEnd.set(Calendar.MONTH, schedule.get(Calendar.MONTH));
                                scheduleEnd.set(Calendar.DAY_OF_MONTH, schedule.get(Calendar.DAY_OF_MONTH));

                                ScheduledSubmission scheduledSubmission = new ScheduledSubmission();
                                scheduledSubmission.setScheduleDefinitionId(scheduleDefinition.getId());
                                scheduledSubmission.setProcess(scheduleDefinition.getProcess());
                                scheduledSubmission.setStartTime(scheduleStart.getTime());
                                scheduledSubmission.setEndTime(scheduleEnd.getTime());
                                scheduledSubmission.setTolerance(scheduleDefinition.getTolerance());
                                scheduledSubmissions.add(scheduledSubmission);

                                schedule.add(Calendar.DAY_OF_MONTH, 1);
                                // schedule.add(Calendar.MONTH, daily.monthsNumber);
                            }
                        }
                        // yet to be implemented
                    }
                    break;
                case "hourly":

                    scheduleStart = Calendar.getInstance();
                    scheduleStart.setTime(scheduleDefinition.getStartTime());

                    scheduleEnd = Calendar.getInstance();
                    scheduleEnd.setTime(scheduleDefinition.getEndTime());

                    startDate = scheduleDefinition.getScheduleStartDate();
                    endDate = scheduleDefinition.getScheduleEndDate();

                    schedule = Calendar.getInstance();
                    schedule.setTime(scheduleStart.getTime());
                    // schedule.add(Calendar.HOUR_OF_DAY, daily.recurEveryHour);
                    scheduleAfterDate = Calendar.getInstance();
                    scheduleAfterDate.setTime(endDate);
                    scheduleAfterDate.add(Calendar.DATE, 1);
                    // scheduleAfterDate.add(Calendar.HOUR_OF_DAY, daily.timeRecurrence);
                    afterEndDate = scheduleAfterDate.getTime();

                    while ((schedule.getTime().after(startDate) || schedule.getTime().equals(startDate))
                            && schedule.getTime().before(afterEndDate)) {

                        scheduleStart.set(Calendar.YEAR, schedule.get(Calendar.YEAR));
                        scheduleStart.set(Calendar.MONTH, schedule.get(Calendar.MONTH));
                        scheduleStart.set(Calendar.DAY_OF_MONTH, schedule.get(Calendar.DAY_OF_MONTH));
                        scheduleStart.set(Calendar.HOUR_OF_DAY, schedule.get(Calendar.HOUR_OF_DAY));

                        scheduleEnd.set(Calendar.YEAR, schedule.get(Calendar.YEAR));
                        scheduleEnd.set(Calendar.MONTH, schedule.get(Calendar.MONTH));
                        scheduleEnd.set(Calendar.DAY_OF_MONTH, schedule.get(Calendar.DAY_OF_MONTH));
                        scheduleEnd.set(Calendar.HOUR_OF_DAY, schedule.get(Calendar.HOUR_OF_DAY));

                        ScheduledSubmission scheduledSubmission = new ScheduledSubmission();
                        scheduledSubmission.setScheduleDefinitionId(scheduleDefinition.getId());
                        scheduledSubmission.setProcess(scheduleDefinition.getProcess());
                        scheduledSubmission.setStartTime(scheduleStart.getTime());
                        scheduledSubmission.setEndTime(scheduleEnd.getTime());
                        scheduledSubmission.setTolerance(scheduleDefinition.getTolerance());
                        scheduledSubmissions.add(scheduledSubmission);

                        schedule.add(Calendar.HOUR_OF_DAY, daily.timeRecurrence);
                        // schedule.add(Calendar.MONTH, daily.monthsNumber);
                    }

                    break;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            break;
        case 'W':
            try {
                Calendar scheduleStart = null;
                Calendar scheduleEnd = null;
                Date startDate = null;
                Date endDate = null;
                Calendar schedule = null;
                Date afterEndDate = null;
                Calendar scheduleAfterDate = Calendar.getInstance();

                Weekly weekly = new ObjectMapper().readValue(scheduleDefinition.getSettings(), Weekly.class);
                scheduleStart = Calendar.getInstance();
                scheduleStart.setTime(scheduleDefinition.getStartTime());
                scheduleEnd = Calendar.getInstance();
                scheduleEnd.setTime(scheduleDefinition.getEndTime());
                startDate = scheduleDefinition.getScheduleStartDate();
                endDate = scheduleDefinition.getScheduleEndDate();

                schedule = Calendar.getInstance();
                schedule.setTime(startDate);
                schedule.add(Calendar.MINUTE, 10);

                scheduleAfterDate.setTime(endDate);
                scheduleAfterDate.add(Calendar.DATE, 1);
                afterEndDate = scheduleAfterDate.getTime();

                // schedule.add(Calendar.DAY_OF_WEEK_IN_MONTH, 0);
                Boolean[] days = weekly.days;
                while (schedule.getTime().after(startDate) && schedule.getTime().before(afterEndDate)) {                    
                    if (days[0]) {
                        schedule.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                        if(schedule.getTime().before(afterEndDate))
                            setScheduleDateTime(schedule, scheduleStart, scheduleEnd, scheduledSubmissions,
                                scheduleDefinition);
                    }
                    if (days[1]) {
                        schedule.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
                        if(schedule.getTime().before(afterEndDate))
                            setScheduleDateTime(schedule, scheduleStart, scheduleEnd, scheduledSubmissions,
                                scheduleDefinition);
                    }
                    if (days[2]) {
                        schedule.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
                        if(schedule.getTime().before(afterEndDate))
                            setScheduleDateTime(schedule, scheduleStart, scheduleEnd, scheduledSubmissions,
                                scheduleDefinition);
                    }
                    if (days[3]) {
                        schedule.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                        if(schedule.getTime().before(afterEndDate))
                            setScheduleDateTime(schedule, scheduleStart, scheduleEnd, scheduledSubmissions,
                                scheduleDefinition);
                    }
                    if (days[4]) {
                        schedule.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
                        if(schedule.getTime().before(afterEndDate))
                            setScheduleDateTime(schedule, scheduleStart, scheduleEnd, scheduledSubmissions,
                                scheduleDefinition);
                    }
                    if (days[5]) {
                        schedule.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                        if(schedule.getTime().before(afterEndDate))
                            setScheduleDateTime(schedule, scheduleStart, scheduleEnd, scheduledSubmissions,
                                scheduleDefinition);
                    } 
                    if (days[6]) {
                        schedule.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                        if(schedule.getTime().before(afterEndDate))
                            setScheduleDateTime(schedule, scheduleStart, scheduleEnd, scheduledSubmissions,
                                scheduleDefinition);
                    }                   
                    schedule.add(Calendar.DAY_OF_WEEK_IN_MONTH, weekly.recurEvery);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            break;
        case 'M':
            try {
                Calendar scheduleStart = null;
                Calendar scheduleEnd = null;
                Date startDate = null;
                Date endDate = null;
                Calendar schedule = null;
                Date afterEndDate = null;
                Calendar scheduleAfterDate = null;
                Monthly monthly = new ObjectMapper().readValue(scheduleDefinition.getSettings(), Monthly.class);
                switch (monthly.option) {
                case "day":
                    scheduleStart = Calendar.getInstance();
                    scheduleStart.setTime(scheduleDefinition.getStartTime());
                    scheduleEnd = Calendar.getInstance();

                    scheduleEnd.setTime(scheduleDefinition.getEndTime());
                    startDate = scheduleDefinition.getScheduleStartDate();
                    endDate = scheduleDefinition.getScheduleEndDate();

                    schedule = Calendar.getInstance();
                    schedule.setTime(startDate);
                    schedule.set(Calendar.DATE, monthly.daysNumber);
                    schedule.add(Calendar.MINUTE, 30);
                    scheduleAfterDate = Calendar.getInstance();
                    scheduleAfterDate.setTime(endDate);
                    scheduleAfterDate.add(Calendar.DATE, 1);
                    afterEndDate = scheduleAfterDate.getTime();

                    while (schedule.getTime().after(startDate) && schedule.getTime().before(afterEndDate)) {

                        scheduleStart.set(Calendar.YEAR, schedule.get(Calendar.YEAR));
                        scheduleStart.set(Calendar.MONTH, schedule.get(Calendar.MONTH));
                        scheduleStart.set(Calendar.DAY_OF_MONTH, schedule.get(Calendar.DAY_OF_MONTH));

                        scheduleEnd.set(Calendar.YEAR, schedule.get(Calendar.YEAR));
                        scheduleEnd.set(Calendar.MONTH, schedule.get(Calendar.MONTH));
                        scheduleEnd.set(Calendar.DAY_OF_MONTH, schedule.get(Calendar.DAY_OF_MONTH));

                        ScheduledSubmission scheduledSubmission = new ScheduledSubmission();
                        scheduledSubmission.setScheduleDefinitionId(scheduleDefinition.getId());
                        scheduledSubmission.setProcess(scheduleDefinition.getProcess());
                        scheduledSubmission.setStartTime(scheduleStart.getTime());
                        scheduledSubmission.setEndTime(scheduleEnd.getTime());
                        scheduledSubmission.setTolerance(scheduleDefinition.getTolerance());
                        scheduledSubmissions.add(scheduledSubmission);

                        schedule.set(Calendar.DATE, monthly.daysNumber);
                        schedule.add(Calendar.MONTH, monthly.monthsNumber);
                    }

                    break;
                case "the":
                    scheduleStart = Calendar.getInstance();
                    scheduleStart.setTime(scheduleDefinition.getStartTime());
                    scheduleEnd = Calendar.getInstance();

                    scheduleEnd.setTime(scheduleDefinition.getEndTime());
                    startDate = scheduleDefinition.getScheduleStartDate();
                    endDate = scheduleDefinition.getScheduleEndDate();

                    schedule = Calendar.getInstance();
                    schedule.setTime(startDate);
                    // schedule.add(Calendar.MONTH, monthly.every);
                    schedule.add(Calendar.MINUTE, 30);
                    scheduleAfterDate = Calendar.getInstance();
                    scheduleAfterDate.setTime(endDate);
                    scheduleAfterDate.add(Calendar.DATE, 1);
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

                        ScheduledSubmission scheduledSubmission = new ScheduledSubmission();
                        scheduledSubmission.setScheduleDefinitionId(scheduleDefinition.getId());
                        scheduledSubmission.setProcess(scheduleDefinition.getProcess());
                        scheduledSubmission.setStartTime(scheduleStart.getTime());
                        scheduledSubmission.setEndTime(scheduleEnd.getTime());
                        scheduledSubmission.setTolerance(scheduleDefinition.getTolerance());
                        scheduledSubmissions.add(scheduledSubmission);

                        schedule.add(Calendar.MONTH, monthly.every);
                        schedule.set(Calendar.DAY_OF_WEEK_IN_MONTH, monthly.ocurrence);
                        setDayName(monthly.day, schedule);
                    }

                    break;
                case "lcd":
                    scheduleStart = Calendar.getInstance();
                    scheduleStart.setTime(scheduleDefinition.getStartTime());
                    scheduleEnd = Calendar.getInstance();
                    scheduleEnd.setTime(scheduleDefinition.getEndTime());
                    startDate = scheduleDefinition.getScheduleStartDate();
                    endDate = scheduleDefinition.getScheduleEndDate();
                    int intCounter = monthly.monthsLCD;
                    schedule = Calendar.getInstance();
                    schedule.setTime(startDate);
                    schedule.add(Calendar.MINUTE, 30);
                    scheduleAfterDate = Calendar.getInstance();
                    scheduleAfterDate.setTime(endDate);
                    scheduleAfterDate.add(Calendar.DATE, 1);
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

                                ScheduledSubmission scheduledSubmission = new ScheduledSubmission();
                                scheduledSubmission.setScheduleDefinitionId(scheduleDefinition.getId());
                                scheduledSubmission.setProcess(scheduleDefinition.getProcess());
                                scheduledSubmission.setStartTime(scheduleStart.getTime());
                                scheduledSubmission.setEndTime(scheduleEnd.getTime());
                                scheduledSubmission.setTolerance(scheduleDefinition.getTolerance());
                                scheduledSubmissions.add(scheduledSubmission);
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
            break;
        case 'Y':
            System.out.println("Not yet implemented");
            break;
        }
        scheduledSubmissionRepository.saveAll(scheduledSubmissions);
    }

}
