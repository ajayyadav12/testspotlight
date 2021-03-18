package com.ge.finance.spotlight.cronjobs;

import com.ge.finance.spotlight.models.ProcessStep;
import com.ge.finance.spotlight.models.SubmissionStep;
import com.ge.finance.spotlight.repositories.*;
import com.ge.finance.spotlight.services.SpotlightEmailService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SubmissionCronJobUnitTest {

    @Captor private ArgumentCaptor<Long> processStepArgumentCaptor;

    @Mock private ScheduledSubmissionRepository scheduledSubmissionRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private SpotlightEmailService spotlightEmailService;
    @Mock private SubmissionRepository submissionRepository;
    @Mock private ProcessRepository processRepository;
    @Mock private ProcessStepRepository processStepRepository;
    @Mock private AnalyticsReportRepository analyticsReportRepository;
    @Mock private ScheduleReportRepository scheduleReportRepository;
    @Mock private ScheduleDefinitionRepository scheduleDefinitionRepository;
    @Mock private ProcessUserRepository processUserRepository;
    @Mock private SubmissionStepRepository submissionStepRepository;
    @Mock private MessageGatewayRepository messageGatewayRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationLogRepository notificationLogRepository;
    @InjectMocks SubmissionCronJob submissionCronJob;

    @Test
    public void testCalculateStepDuration() {
        long now = System.currentTimeMillis();
        SubmissionStep step1 = new SubmissionStep();
        step1.setStartTime(new Date(now));
        step1.setEndTime(new Date(now + 60_000)); // 1 minute
        SubmissionStep step2 = new SubmissionStep();
        step2.setStartTime(new Date(now + 120_000));
        step2.setEndTime(new Date(now + 360_000)); // 3 minutes
        ProcessStep processStep = Mockito.mock(ProcessStep.class);
        when(processStep.getId()).thenReturn(1L);
        doNothing().when(processStep).setDuration(any(Long.class));
        List<ProcessStep> processSteps = List.of(processStep);
        when(processStepRepository.findAll()).thenReturn(processSteps);
        when(processStepRepository.save(any(ProcessStep.class))).thenReturn(new ProcessStep());
        when(submissionStepRepository.findByProcessStepIdAndEndTimeIsNotNull(any(Long.class))).thenReturn(List.of(step1, step2));
        submissionCronJob.calculateStepDuration();
        verify(processStep).setDuration(processStepArgumentCaptor.capture());
        assertEquals(2L, (long)processStepArgumentCaptor.getValue());
        verify(processStepRepository).save(processStep);
    }

}
