package com.ge.finance.spotlight.controllers;

import com.ge.finance.spotlight.exceptions.NotFoundException;
import com.ge.finance.spotlight.models.NotificationTemplate;
import com.ge.finance.spotlight.repositories.NotificationTemplateRepository;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NotificationTemplateControllerUnitTest {

    @Mock private NotificationTemplateRepository notificationTemplateRepository;    
    @InjectMocks private NotificationTemplateController notificationTemplateController;

    @Test
    public void testGetNotificationTemplates() {
        when(notificationTemplateRepository.findAll()).thenReturn(Collections.emptyList());
        assertNotNull(notificationTemplateController.index());
    }

    @Test
    public void testCreateNotificationTemplate() {
        NotificationTemplate notificationTemplateToSave = new NotificationTemplate();
        NotificationTemplate notificationTemplateSaved = new NotificationTemplate();
        when(notificationTemplateRepository.save(any(NotificationTemplate.class))).thenReturn(notificationTemplateSaved);
        assertNotNull(notificationTemplateController.create(notificationTemplateToSave));
        verify(notificationTemplateRepository).save(notificationTemplateToSave);
    }

    @Test
    public void testGetExistingNotificationTemplate() {
        NotificationTemplate notificationTemplate = new NotificationTemplate();
        when(notificationTemplateRepository.findById(any(Long.class))).thenReturn(Optional.of(notificationTemplate));
        assertEquals(notificationTemplateController.get(1L), notificationTemplate);
    }

    @Test(expected = NotFoundException.class)
    public void testGetNonExistingNotificationTemplate() {
        when(notificationTemplateRepository.findById(any(Long.class))).thenReturn(Optional.empty());
        notificationTemplateController.get(1L);
    }

    @Test
    public void testUpdateNotificationTemplateForExistingNotificationTemplate() {
        Long notificationTemplateId = 1L;
        NotificationTemplate notificationTemplateToUpdate = Mockito.mock(NotificationTemplate.class);
        NotificationTemplate notificationTemplateUpdated = new NotificationTemplate();
        when(notificationTemplateRepository.existsById(any(Long.class))).thenReturn(true);
        when(notificationTemplateRepository.save(any(NotificationTemplate.class))).thenReturn(notificationTemplateUpdated);
        assertNotNull(notificationTemplateController.update(notificationTemplateId, notificationTemplateToUpdate));
        verify(notificationTemplateToUpdate).setId(notificationTemplateId);
        verify(notificationTemplateRepository).save(notificationTemplateToUpdate);
    }

    @Test(expected = NotFoundException.class)
    public void testUpdateNotificationTemplatenameForNonExistingNotificationTemplate() {
        NotificationTemplate notificationTemplateToUpdate = new NotificationTemplate();
        when(notificationTemplateRepository.existsById(any(Long.class))).thenReturn(false);
        notificationTemplateController.update(1L, notificationTemplateToUpdate);
    }

    @Test
    public void testDeleteExistingNotificationTemplate() {
        NotificationTemplate notificationTemplateToDelete = mock(NotificationTemplate.class);        
        when(notificationTemplateRepository.findById(any(Long.class))).thenReturn(Optional.of(notificationTemplateToDelete));
        doNothing().when(notificationTemplateRepository).deleteById(1L);
        assertNotNull(notificationTemplateController.delete(1L));
        verify(notificationTemplateRepository).deleteById(1L);
    }

    @Test(expected = NotFoundException.class)
    public void testDeleteNonExistingNotificationTemplate() {        
        when(notificationTemplateRepository.findById(any(Long.class))).thenReturn(Optional.empty());
        notificationTemplateController.delete(1L);
    }

}
