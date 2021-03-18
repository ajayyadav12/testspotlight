package com.ge.finance.spotlight.controllers;

import com.ge.finance.spotlight.exceptions.ForbiddenException;
import com.ge.finance.spotlight.models.ModuleFilter;
import com.ge.finance.spotlight.repositories.ModuleFilterRepository;
import com.ge.finance.spotlight.repositories.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ModuleFilterControllerUnitTest {

    @Mock private ModuleFilterRepository moduleFilterRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private ModuleFilterController moduleFilterController;

    @Test
    public void testUpdateMultipleEmptyList() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(999999999L, null, Collections.emptyList());
        Collection<ModuleFilter> filters = moduleFilterController.updateMultiple(Collections.emptyList(), authentication);
        assertNotNull(filters);
        assertEquals(0, filters.size());
    }

    @Test(expected = ForbiddenException.class)
    public void testUpdateMultipleForbidden() {
        when(moduleFilterRepository.existsAllForIdListAndSso(anyList(), any(Long.class))).thenReturn(false);
        Authentication authentication = new UsernamePasswordAuthenticationToken(999999999L, null, Collections.emptyList());
        ModuleFilter moduleFilter = new ModuleFilter();
        moduleFilter.setId(1L);
        moduleFilterController.updateMultiple(List.of(moduleFilter), authentication);
    }

    @Test
    public void testUpdateMultiple() {
        ModuleFilter filterUpdated = new ModuleFilter();
        filterUpdated.setId(1L);
        when(moduleFilterRepository.existsAllForIdListAndSso(anyList(), any(Long.class))).thenReturn(true);
        when(moduleFilterRepository.saveAll(anyList())).thenReturn(List.of(filterUpdated));
        ModuleFilter filterToUpdate = new ModuleFilter();
        filterToUpdate.setId(1L);
        Collection<ModuleFilter> filtersToUpdate = List.of(filterToUpdate);
        Authentication authentication = new UsernamePasswordAuthenticationToken(999999999L, null, Collections.emptyList());
        Collection<ModuleFilter> filters = moduleFilterController.updateMultiple(filtersToUpdate, authentication);
        verify(moduleFilterRepository).saveAll(filtersToUpdate);
        assertNotNull(filters);
        assertEquals(1, filters.size());
    }

}