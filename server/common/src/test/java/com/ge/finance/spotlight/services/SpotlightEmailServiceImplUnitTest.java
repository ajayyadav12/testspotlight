package com.ge.finance.spotlight.services;

import com.ge.finance.spotlight.dto.EmailModel;
import com.ge.finance.spotlight.models.*;
import com.ge.finance.spotlight.repositories.NotificationLogRepository;
import com.ge.finance.spotlight.repositories.NotificationTemplateRepository;
import com.ge.finance.spotlight.repositories.ProcessRepository;
import com.ge.finance.spotlight.repositories.ScheduleReportRepository;
import com.ge.finance.spotlight.repositories.SubmissionRepository;
import com.ge.finance.spotlight.services.SpotlightEmailServiceImpl;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SpotlightEmailServiceImplUnitTest {

    @Captor private ArgumentCaptor<Long> processStepArgumentCaptor;

    @Mock private NotificationTemplateRepository notificationTemplateRepository;
    @Mock private NotificationLogRepository notificationLogRepository;
    @Mock private JavaMailSender javaMailSender;
    @Mock private NotificationLog notificationLog;
    @Mock private ProcessRepository processRepository;
    @Mock private ScheduleReportRepository scheduleReportRepository;
    @Mock private SubmissionRepository submissionRepository;   

    @InjectMocks private SpotlightEmailServiceImpl spotlightEmailServiceImpl;

    @Test
    public void testGenericSend() {
        NotificationTemplate notificationTemplate = new NotificationTemplate();
        notificationTemplate.setId(1l);
        notificationTemplate.setBody("");
        notificationTemplate.setSubject("");
        when(notificationTemplateRepository.findById(anyLong())).thenReturn(Optional.of(notificationTemplate));
        
        EmailModel emailModel = new EmailModel(1l, "test@ge.com", false);        

        SpotlightEmailServiceImpl spotlightEmailServiceImplMock = spy(spotlightEmailServiceImpl);
        spotlightEmailServiceImplMock.genericSend(emailModel);
        verify(spotlightEmailServiceImplMock , times(1)).sendEmail(anyString(), anyString(), anyString());
        verify(spotlightEmailServiceImplMock , times(2)).keywordReplacement(anyString(), any(), any());
    }

    @Test
    public void testGenericSendSMS() {
        NotificationTemplate notificationTemplate = new NotificationTemplate();
        notificationTemplate.setId(1l);
        notificationTemplate.setBody("");
        notificationTemplate.setSubject("");
        when(notificationTemplateRepository.findById(anyLong())).thenReturn(Optional.of(notificationTemplate));
        
        EmailModel emailModel = new EmailModel(1l, "test@ge.com", true);        

        SpotlightEmailServiceImpl spotlightEmailServiceImplMock = spy(spotlightEmailServiceImpl);
        spotlightEmailServiceImplMock.genericSend(emailModel);
        verify(spotlightEmailServiceImplMock , times(1)).sendEmail(anyString(), anyString(), anyString());
        verify(spotlightEmailServiceImplMock , times(1)).keywordReplacement(anyString(), any(), any());
    } 

    @Test
    public void testKeywordReplacementUnknownFields() {
        EmailModel emailModel = new EmailModel(1l, "test@ge.com", true);                        

        SpotlightEmailServiceImpl spotlightEmailServiceImplMock = spy(spotlightEmailServiceImpl);
        String result = spotlightEmailServiceImplMock.keywordReplacement("emailModel${}", emailModel, null);

        assertEquals(result, "emailModel");
        
    }

    @Test
    public void testKeywordReplacement() {
        String body = "${PARENT_SUBMISSION_ID} ${PARENT_SUBMISSION_STATUS}";
        EmailModel emailModel = new EmailModel(1l, "test@ge.com", true);    
        emailModel.parentSubmission = new ParentSubmission();
        emailModel.parentSubmission.setId(1l);
        Status status = new Status();
        status.setName("success");
        emailModel.parentSubmission.setStatus(status);

        SpotlightEmailServiceImpl spotlightEmailServiceImplMock = spy(spotlightEmailServiceImpl);
        String result = spotlightEmailServiceImplMock.keywordReplacement(body, emailModel, null);

        assertEquals(result, "1 success");
        
    }

}
