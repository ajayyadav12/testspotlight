package com.ge.finance.spotlight.services;

import com.ge.finance.spotlight.models.*;
import com.ge.finance.spotlight.models.Process;
import com.ge.finance.spotlight.models.System;
import com.ge.finance.spotlight.repositories.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ProcessImportServiceUnitTest {

    @Captor private ArgumentCaptor<ClosePhase> closePhaseArgumentCaptor;
    @Captor private ArgumentCaptor<FeedType> feedTypeArgumentCaptor;
    @Captor private ArgumentCaptor<MessageGateway> messageGatewayArgumentCaptor;
    @Captor private ArgumentCaptor<Notification> notificationArgumentCaptor;
    @Captor private ArgumentCaptor<ProcessType> processTypeArgumentCaptor;
    @Captor private ArgumentCaptor<System> systemArgumentCaptor;
    @Captor private ArgumentCaptor<User> userArgumentCaptor;

    @Mock private ClosePhaseRepository closePhaseRepository;
    @Mock private FeedTypeRepository feedTypeRepository;
    @Mock private MessageGatewayRepository messageGatewayRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private ProcessRepository processRepository;
    @Mock private ProcessStepRepository processStepRepository;
    @Mock private ProcessTypeRepository processTypeRepository;
    @Mock private ProcessUserRepository processUserRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private ScheduleDefinitionRepository scheduleDefinitionRepository;
    @Mock private StatusRepository statusRepository;
    @Mock private SystemRepository systemRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private ProcessImportService processImportService;

    @Test
    public void testSimpleRandomStringGenerator() {
        String randomString = processImportService.simpleRandomStringGenerator();
        assertNotNull(randomString);
        assertEquals(6, randomString.length());
    }

    @Test
    public void testGetClosePhaseByName() {
        String closePhaseName = "closePhaseName";
        ClosePhase closePhase = new ClosePhase();
        closePhase.setName(closePhaseName);
        when(closePhaseRepository.findFirstByName(closePhaseName)).thenReturn(Optional.of(closePhase));
        ClosePhase getClosePhase = processImportService.getOrCreateClosePhaseByName(closePhaseName);
        assertNotNull(getClosePhase);
        assertEquals(closePhaseName, getClosePhase.getName());
    }

    @Test
    public void testCreateClosePhaseByName() {
        String closePhaseName = "closePhaseName";
        when(closePhaseRepository.findFirstByName(closePhaseName)).thenReturn(Optional.empty());
        processImportService.getOrCreateClosePhaseByName(closePhaseName);
        verify(closePhaseRepository).save(closePhaseArgumentCaptor.capture());
        assertEquals(closePhaseName, closePhaseArgumentCaptor.getValue().getName());
    }

    @Test
    public void testGetFeedTypeByName() {
        String feedTypeName = "feedTypeName";
        FeedType feedType = new FeedType();
        feedType.setName(feedTypeName);
        when(feedTypeRepository.findFirstByName(feedTypeName)).thenReturn(Optional.of(feedType));
        FeedType getFeedType = processImportService.getOrCreateFeedTypeByName(feedTypeName);
        assertNotNull(getFeedType);
        assertEquals(feedTypeName, getFeedType.getName());
    }

    @Test
    public void testCreateFeedTypeByName() {
        String feedTypeName = "feedTypeName";
        when(feedTypeRepository.findFirstByName(feedTypeName)).thenReturn(Optional.empty());
        processImportService.getOrCreateFeedTypeByName(feedTypeName);
        verify(feedTypeRepository).save(feedTypeArgumentCaptor.capture());
        assertEquals(feedTypeName, feedTypeArgumentCaptor.getValue().getName());
    }

    @Test
    public void testGetMessageGatewayByCarrierAndName() {
        String carrier = "carrier";
        String country = "country";
        String gateway = "gateway";
        MessageGateway messageGateway = new MessageGateway();
        messageGateway.setCarrier(carrier);
        messageGateway.setCountry(country);
        messageGateway.setGateway(gateway);
        when(messageGatewayRepository.findFirstByCarrierAndCountry(carrier, country)).thenReturn(Optional.of(messageGateway));
        MessageGateway getMessageGateway = processImportService.getOrCreateMessageGatewayByCarrierAndCountry(messageGateway);
        assertNotNull(getMessageGateway);
        assertEquals(String.format("%s~%s", carrier, country), getMessageGateway.getCarrier());
        assertEquals(country, getMessageGateway.getCountry());
        assertEquals(gateway, getMessageGateway.getGateway());
    }

    @Test
    public void testCreateMessageGatewayByCarrierAndName() {
        String carrier = "carrier";
        String country = "country";
        String gateway = "gateway";
        MessageGateway messageGateway = new MessageGateway();
        messageGateway.setCarrier(carrier);
        messageGateway.setCountry(country);
        messageGateway.setGateway(gateway);
        when(messageGatewayRepository.findFirstByCarrierAndCountry(carrier, country)).thenReturn(Optional.empty());
        processImportService.getOrCreateMessageGatewayByCarrierAndCountry(messageGateway);
        verify(messageGatewayRepository).save(messageGatewayArgumentCaptor.capture());
        assertEquals(String.format("%s~%s", carrier, country), messageGatewayArgumentCaptor.getValue().getCarrier());
        assertEquals(country, messageGatewayArgumentCaptor.getValue().getCountry());
        assertEquals(gateway, messageGatewayArgumentCaptor.getValue().getGateway());
    }

    @Test
    public void testGetProcessByName() {
        String processName = "processName";
        Process process = new Process();
        process.setName(processName);
        when(processRepository.findFirstByName(processName)).thenReturn(Optional.of(process));
        Process getProcess = processImportService.getOrCreateProcessByName(process);
        assertNotNull(getProcess);
        assertEquals(processName, getProcess.getName());
    }

    //TODO: public void testCreateProcessByName()

    @Test
    public void testGetProcessTypeByName() {
        String processTypeName = "processTypeName";
        ProcessType processType = new ProcessType();
        processType.setName(processTypeName);
        when(processTypeRepository.findFirstByName(processTypeName)).thenReturn(Optional.of(processType));
        ProcessType getProcessType = processImportService.getOrCreateProcessTypeByName(processTypeName);
        assertNotNull(getProcessType);
        assertEquals(processTypeName, getProcessType.getName());
    }

    @Test
    public void testCreateProcessTypeByName() {
        String processTypeName = "processTypeName";
        when(processTypeRepository.findFirstByName(processTypeName)).thenReturn(Optional.empty());
        processImportService.getOrCreateProcessTypeByName(processTypeName);
        verify(processTypeRepository).save(processTypeArgumentCaptor.capture());
        assertEquals(processTypeName, processTypeArgumentCaptor.getValue().getName());
    }

    @Test
    public void testGetSystemByName() {
        String systemName = "systemName";
        User appOwner = new User();
        ClosePhase closePhase = new ClosePhase();
        System system = new System();
        system.setName(systemName);
        system.setAppOwner(appOwner);
        system.setClosePhase(closePhase);
        when(systemRepository.findFirstByName(systemName)).thenReturn(Optional.of(system));
        System getSystem = processImportService.getOrCreateSystemByName(system);
        assertNotNull(getSystem);
        assertEquals(systemName, getSystem.getName());
        assertEquals(appOwner, getSystem.getAppOwner());
        assertEquals(closePhase, getSystem.getClosePhase());
    }

    @Test
    public void testCreateSystemByName() {
        ProcessImportService processImportServiceSpy = spy(processImportService);
        String systemName = "systemName";
        Long sso = 1L;
        User appOwner = new User();
        appOwner.setSso(sso);
        when(userRepository.findOptionalBySSO(sso)).thenReturn(Optional.of(appOwner));
        String closePhaseName = "closePhaseName";
        ClosePhase closePhase = new ClosePhase();
        closePhase.setName(closePhaseName);
        when(closePhaseRepository.findFirstByName(closePhaseName)).thenReturn(Optional.of(closePhase));
        System system = new System();
        system.setName(systemName);
        system.setAppOwner(appOwner);
        system.setClosePhase(closePhase);
        processImportServiceSpy.getOrCreateSystemByName(system);
        verify(processImportServiceSpy).getOrCreateUserBySSO(appOwner);
        verify(processImportServiceSpy).getOrCreateClosePhaseByName(closePhaseName);
        verify(systemRepository).save(systemArgumentCaptor.capture());
        assertEquals(systemName, systemArgumentCaptor.getValue().getName());
        assertEquals(appOwner, systemArgumentCaptor.getValue().getAppOwner());
        assertEquals(closePhase, systemArgumentCaptor.getValue().getClosePhase());
    }

    @Test
    public void testGetUserBySSO() {
        Long sso = 1L;
        User user = new User();
        user.setSso(sso);
        when(userRepository.findOptionalBySSO(sso)).thenReturn(Optional.of(user));
        User getUser = processImportService.getOrCreateUserBySSO(user);
        assertNotNull(getUser);
        assertEquals(sso, getUser.getSso());
    }

    @Test
    public void testCreateUserBySSO() {
        ProcessImportService processImportServiceSpy = spy(processImportService);
        MessageGateway messageGateway = new MessageGateway();
        messageGateway.setCarrier("carrier");
        messageGateway.setCountry("country");
        messageGateway.setGateway("gateway");
        when(messageGatewayRepository.findFirstByCarrierAndCountry(anyString(), anyString())).thenReturn(Optional.of(messageGateway));
        String description = "description";
        Role role = new Role();
        role.setDescription(description);
        when(roleRepository.findFirstByDescription(anyString())).thenReturn(Optional.of(role));
        String name = "name";
        Long sso = 1L;
        Long phoneNumber = 2L;
        User user = new User();
        user.setName(name);
        user.setSso(sso);
        user.setRole(role);
        user.setCarrier(messageGateway);
        user.setPhoneNumber(phoneNumber);
        processImportServiceSpy.getOrCreateUserBySSO(user);
        verify(processImportServiceSpy).getOrCreateMessageGatewayByCarrierAndCountry(messageGateway);
        verify(userRepository).save(userArgumentCaptor.capture());
        assertEquals(name, userArgumentCaptor.getValue().getName());
        assertEquals(sso, userArgumentCaptor.getValue().getSso());
        assertEquals(role, userArgumentCaptor.getValue().getRole());
        assertEquals(messageGateway, userArgumentCaptor.getValue().getCarrier());
        assertEquals(phoneNumber, userArgumentCaptor.getValue().getPhoneNumber());
    }

    @Test
    public void testCreateNotification() {
        Long processId = 1L;
        String processStepName = "processStepName";
        ProcessStep processStep = new ProcessStep();
        processStep.setName(processStepName);
        when(processStepRepository.findFirstByNameAndProcessId(processStepName, processId)).thenReturn(Optional.of(processStep));
        String statusName = "statusName";
        Status status = new Status();
        status.setName(statusName);
        when(statusRepository.findFirstByName(statusName)).thenReturn(Optional.of(status));
        String additionalEmails = "additionalEmails";        
        Long createdBySSO = 3L;
        User createdBy = new User();
        createdBy.setSso(createdBySSO);
        when(userRepository.findOptionalBySSO(createdBySSO)).thenReturn(Optional.of(createdBy));
        String escalationType = "escalationType";
        String submissionType = "submissionType";
        Notification notification = new Notification();
        Set<NotificationMobile> userMobile = new HashSet<NotificationMobile>();
        notification.setProcessId(processId);
        notification.setProcessStep(processStep);
        notification.setStatus(status);
        notification.setAdditionalEmails(additionalEmails);
        notification.setEnableTextMessaging(true);
        notification.setUserMobiles(userMobile);
        notification.setCreatedBy(createdBy);
        notification.setEscalationType(escalationType);
        notification.setSubmissionType(submissionType);
        ProcessImportService processImportServiceSpy = spy(processImportService);
        processImportServiceSpy.createNotification(notification, processId);
        verify(processImportServiceSpy, times(1)).getOrCreateUserBySSO(any());
        verify(notificationRepository).save(notificationArgumentCaptor.capture());
        assertEquals(processId, notificationArgumentCaptor.getValue().getProcessId());
        assertEquals(processStep, notificationArgumentCaptor.getValue().getProcessStep());
        assertEquals(status, notificationArgumentCaptor.getValue().getStatus());
        assertEquals(additionalEmails, notificationArgumentCaptor.getValue().getAdditionalEmails());
        assertTrue(notificationArgumentCaptor.getValue().getEnableTextMessaging());
        assertEquals(userMobile, notificationArgumentCaptor.getValue().getUserMobiles());
        assertEquals(createdBy, notificationArgumentCaptor.getValue().getCreatedBy());
        assertEquals(escalationType, notificationArgumentCaptor.getValue().getEscalationType());
        assertEquals(submissionType, notificationArgumentCaptor.getValue().getSubmissionType());
    }

}
