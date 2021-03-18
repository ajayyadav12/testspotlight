package com.ge.finance.spotlight.services;

import com.ge.finance.spotlight.dto.ScheduleDefinitionDTO;
import com.ge.finance.spotlight.models.ScheduleDefinition;

public interface ScheduledSubmissionService {

    void setupScheduledSubmissions(ScheduleDefinition scheduleDefinition,
            ScheduleDefinitionDTO scheduleDefinitionRequest);

}
