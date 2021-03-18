package com.ge.finance.spotlight.services;

import com.ge.finance.spotlight.dto.EmailModel;
import com.ge.finance.spotlight.models.*;
import com.ge.finance.spotlight.models.Process;
import com.ge.finance.spotlight.models.System;
import com.ge.finance.spotlight.repositories.*;
import com.sun.tools.javac.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SubmissionStepServiceUnitTest {

    @Captor ArgumentCaptor<EmailModel> argumentCaptorEmailModel;

    @Mock private MessageGatewayRepository messageGatewayRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private ParentSubmissionRepository parentSubmissionRepository;
    @Mock private ProcessRepository processRepository;
    @Mock private ProcessParentChildRepository processParentChildRepository;
    @Mock private ProcessStepRepository processStepRepository;
    @Mock private ScheduledSubmissionRepository scheduledSubmissionRepository;
    @Mock private SpotlightEmailService spotlightEmailService;
    @Mock private StatusRepository statusRepository;
    @Mock private SubmissionRepository submissionRepository;
    @Mock private SubmissionStepRepository submissionStepRepository;

    @InjectMocks private SubmissionStepService submissionStepService;

    @Test
    public void testGetLatestOpenSubmissionStepBySubmissionIdIsPresent() {
        ProcessStep processStep = new ProcessStep();
        processStep.setId(1L);
        processStep.setParallel(false);
        SubmissionStep submissionStep = new SubmissionStep();
        submissionStep.setId(1L);
        submissionStep.setProcessStep(processStep);
        when(submissionStepRepository.findBySubmissionIdAndEndTimeIsNullOrderByStartTimeDesc(anyLong())).thenReturn(List.of(submissionStep));
        Optional<SubmissionStep> optionalSubmissionStep = submissionStepService.getLatestOpenSubmissionStepBySubmissionId(1L, 1L);
        assertTrue(optionalSubmissionStep.isPresent());
    }

    @Test
    public void testGetLatestOpenSubmissionStepBySubmissionIdIsEmpty() {
        when(submissionStepRepository.findBySubmissionIdAndEndTimeIsNullOrderByStartTimeDesc(anyLong())).thenReturn(Collections.emptyList());
        Optional<SubmissionStep> optionalSubmissionStep = submissionStepService.getLatestOpenSubmissionStepBySubmissionId(1L, 1L);
        assertTrue(optionalSubmissionStep.isEmpty());
    }

    @Test
    public void testGetAppOwnerForProcess() {
        Process process = new Process();
        process.setAppOwner(new User());
        Optional<User> appOwner = submissionStepService.getAppOwner(process);
        assertTrue(appOwner.isPresent());
    }

    @Test
    public void testGetAppOwnerForSender() {
        System sender = new System();
        sender.setAppOwner(new User());
        Process process = new Process();
        process.setSender(sender);
        Optional<User> appOwner = submissionStepService.getAppOwner(process);
        assertTrue(appOwner.isPresent());
    }

    @Test
    public void testGetAppOwnerEmpty() {
        Process process = new Process();
        Optional<User> appOwner = submissionStepService.getAppOwner(process);
        assertTrue(appOwner.isEmpty());
    }

    @Test
    public void testSendEmailToProcessOwnerOnlyOwner() {
        User appOwner = new User();
        appOwner.setSso(999999999L);
        Process process = new Process();
        process.setAppOwner(appOwner);
        SubmissionStep submissionStep = new SubmissionStep();
        Submission submission = new Submission();
        ProcessStep processStep = new ProcessStep();
        submissionStepService.sendEmailToProcessOwner(1L, submissionStep, process, submission, processStep);
        verify(spotlightEmailService).genericSend(argumentCaptorEmailModel.capture());
        assertEquals("999999999@ge.com", argumentCaptorEmailModel.getValue().to);
        assertEquals(submission, argumentCaptorEmailModel.getValue().submission);
        assertEquals(submissionStep, argumentCaptorEmailModel.getValue().submissionStep);
        assertEquals(processStep, argumentCaptorEmailModel.getValue().processStep);
    }

    @Test
    public void testSendEmailToProcessOwnerOwnerAndUserMobiles() {
        User appOwner = new User();
        appOwner.setSso(999999999L);
        Process process = new Process();
        process.setId(1L);
        process.setAppOwner(appOwner);
        Notification notification = new Notification();
        User user = new User();
        user.setSso(888888888L);
        NotificationMobile notificationMobile = new NotificationMobile();
        notificationMobile.setUser(user);
        notification.setUserMobiles(new HashSet<>());
        notification.getUserMobiles().add(notificationMobile);
        when(notificationRepository.findFirstByProcessIdAndSubmissionTypeAndProcessStepIdIsNullAndStatusIdIsNull(anyLong(), anyString())).thenReturn(Optional.of(notification));
        SubmissionStep submissionStep = new SubmissionStep();
        Submission submission = new Submission();
        ProcessStep processStep = new ProcessStep();
        submissionStepService.sendEmailToProcessOwner(1L, submissionStep, process, submission, processStep);
        verify(spotlightEmailService).genericSend(argumentCaptorEmailModel.capture());
        assertEquals("888888888@ge.com 999999999@ge.com", argumentCaptorEmailModel.getValue().to);
        assertEquals(submission, argumentCaptorEmailModel.getValue().submission);
        assertEquals(submissionStep, argumentCaptorEmailModel.getValue().submissionStep);
        assertEquals(processStep, argumentCaptorEmailModel.getValue().processStep);
    }

    @Test
    public void testSendSMSForUserMobilesNoRequiredSteps() {
        Notification notification = new Notification();
        MessageGateway messageGateway = new MessageGateway();
        messageGateway.setGateway("@at&t.com");
        User user = new User();
        user.setPhoneNumber(5555555555L);
        user.setSso(888888888L);
        user.setCarrier(messageGateway);
        NotificationMobile notificationMobile = new NotificationMobile();
        notificationMobile.setUser(user);
        notification.setSubmissionType(""); // other than required steps
        notification.setUserMobiles(new HashSet<>());
        notification.getUserMobiles().add(notificationMobile);
        SubmissionStep submissionStep = new SubmissionStep();
        ParentSubmission parentSubmission = new ParentSubmission();
        Submission submission = new Submission();
        submissionStepService.sendSMS(notification, submissionStep, parentSubmission, submission, null);
        verify(spotlightEmailService).genericSend(argumentCaptorEmailModel.capture());
        assertEquals("5555555555@at&t.com", argumentCaptorEmailModel.getValue().to);
        assertEquals(submission, argumentCaptorEmailModel.getValue().submission);
        assertEquals(submissionStep, argumentCaptorEmailModel.getValue().submissionStep);
        assertEquals(parentSubmission, argumentCaptorEmailModel.getValue().parentSubmission);
        assertNull(argumentCaptorEmailModel.getValue().processStep);
    }

    @Test
    public void testUpdateSubmissionPeriodYear() {
        Submission submission = new Submission();
        submissionStepService.updateSubmissionPeriod(submission, "Y2020");
        assertEquals(2020, submission.getPeriodYear());
        assertEquals(0, submission.getPeriodQuarter());
        assertEquals(0, submission.getPeriodMonth());
    }

    @Test
    public void testUpdateSubmissionPeriodQuarter() {
        Submission submission = new Submission();
        submissionStepService.updateSubmissionPeriod(submission, "Q20-3");
        assertEquals(2020, submission.getPeriodYear());
        assertEquals(3, submission.getPeriodQuarter());
        assertEquals(0, submission.getPeriodMonth());
    }

    @Test
    public void testUpdateSubmissionPeriodMonth() {
        Submission submission = new Submission();
        submissionStepService.updateSubmissionPeriod(submission, "M20 04");
        assertEquals(2020, submission.getPeriodYear());
        assertEquals(2, submission.getPeriodQuarter());
        assertEquals(4, submission.getPeriodMonth());
    }

    @Test
    public void testUpdateSubmissionPeriodMonthName() {
        Submission submission = new Submission();
        submissionStepService.updateSubmissionPeriod(submission, "Nov 20");
        assertEquals(2020, submission.getPeriodYear());
        assertEquals(4, submission.getPeriodQuarter());
        assertEquals(11, submission.getPeriodMonth());
    }

}
