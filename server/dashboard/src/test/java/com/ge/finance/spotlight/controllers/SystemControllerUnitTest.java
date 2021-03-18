package com.ge.finance.spotlight.controllers;

import com.ge.finance.spotlight.exceptions.ConflictException;
import com.ge.finance.spotlight.exceptions.NotFoundException;
import com.ge.finance.spotlight.models.System;
import com.ge.finance.spotlight.repositories.ProcessRepository;
import com.ge.finance.spotlight.repositories.SystemRepository;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SystemControllerUnitTest {

    @Mock private SystemRepository systemRepository;
    @Mock private ProcessRepository processRepository;
    @InjectMocks private SystemController systemController;

    @Test
    public void testGetSystems() {
        when(systemRepository.findAll()).thenReturn(Collections.emptyList());
        assertNotNull(systemController.index());
    }

    @Test
    public void testCreateSystem() {
        System systemToSave = new System();
        System systemSaved = new System();
        when(systemRepository.save(any(System.class))).thenReturn(systemSaved);
        assertNotNull(systemController.create(systemToSave));
        verify(systemRepository).save(systemToSave);
    }

    @Test
    public void testGetExistingSystem() {
        System system = new System();
        when(systemRepository.findById(any(Long.class))).thenReturn(Optional.of(system));
        assertEquals(systemController.get(1L), system);
    }

    @Test(expected = NotFoundException.class)
    public void testGetNonExistingSystem() {
        when(systemRepository.findById(any(Long.class))).thenReturn(Optional.empty());
        systemController.get(1L);
    }

    @Test
    public void testUpdateSystemForExistingSystem() {
        Long systemId = 1L;
        System systemToUpdate = Mockito.mock(System.class);
        System systemUpdated = new System();
        when(systemRepository.existsById(any(Long.class))).thenReturn(true);
        when(systemRepository.save(any(System.class))).thenReturn(systemUpdated);
        assertNotNull(systemController.update(systemId, systemToUpdate));
        verify(systemToUpdate).setId(systemId);
        verify(systemRepository).save(systemToUpdate);
    }

    @Test(expected = NotFoundException.class)
    public void testUpdateSystemnameForNonExistingSystem() {
        System systemToUpdate = new System();
        when(systemRepository.existsById(any(Long.class))).thenReturn(false);
        systemController.update(1L, systemToUpdate);
    }

    @Test
    public void testDeleteExistingSystemWithNoProcess() {
        System systemToDelete = mock(System.class);
        when(processRepository.existsBySenderIdOrReceiverId(any(Long.class), any(Long.class))).thenReturn(false);
        when(systemRepository.findById(any(Long.class))).thenReturn(Optional.of(systemToDelete));
        doNothing().when(systemRepository).delete(any(System.class));
        assertNotNull(systemController.delete(1L));
        verify(systemRepository).delete(systemToDelete);
    }

    @Test(expected = ConflictException.class)
    public void testDeleteExistingSystemWithProcesses() {
        when(processRepository.existsBySenderIdOrReceiverId(any(Long.class), any(Long.class))).thenReturn(true);
        systemController.delete(1L);
    }

    @Test(expected = NotFoundException.class)
    public void testDeleteNonExistingSystem() {
        when(processRepository.existsBySenderIdOrReceiverId(any(Long.class), any(Long.class))).thenReturn(false);
        when(systemRepository.findById(any(Long.class))).thenReturn(Optional.empty());
        systemController.delete(1L);
    }

}
