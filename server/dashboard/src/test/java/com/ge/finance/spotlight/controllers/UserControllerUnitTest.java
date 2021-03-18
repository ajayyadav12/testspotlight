package com.ge.finance.spotlight.controllers;

import com.ge.finance.spotlight.exceptions.ConflictException;
import com.ge.finance.spotlight.exceptions.NotFoundException;
import com.ge.finance.spotlight.models.User;
import com.ge.finance.spotlight.repositories.ProcessUserRepository;
import com.ge.finance.spotlight.repositories.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserControllerUnitTest {

    @Mock private UserRepository userRepository;
    @Mock private ProcessUserRepository processUserRepository;
    @InjectMocks private UserController userController;

    @Test
    public void testGetUsers() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        Map<String, String> map = new HashMap<String, String>();
        assertNotNull(userController.index(map));
    }

    @Test
    public void testGetMobileUsers() {
        when(userRepository.findByPhoneNumberNotNullAndCarrierNotNullOrderByName()).thenReturn(Collections.emptyList());
        Map<String, String> map = new HashMap<String, String>();
        map.put("mobileUsersOnly", "true");
        assertNotNull(userController.index(map));
    }

    @Test
    public void testCreateUser() {
        User userToSave = new User();
        User userSaved = new User();
        when(userRepository.save(any(User.class))).thenReturn(userSaved);
        assertNotNull(userController.create(userToSave));
        verify(userRepository).save(userToSave);
    }

    @Test
    public void testGetExistingUser() {
        User user = new User();
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));
        assertEquals(userController.getUser(1L, null), user);
    }

    @Test(expected = NotFoundException.class)
    public void testGetNonExistingUser() {
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.empty());
        userController.getUser(1L, null);
    }

    @Test
    public void testUpdateUsernameForExistingUser() {
        Long userId = 1L;
        User userToUpdate = Mockito.mock(User.class);
        User userUpdated = new User();
        when(userRepository.existsById(any(Long.class))).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(userUpdated);
        assertNotNull(userController.updateUserName(userId, userToUpdate, null));
        verify(userToUpdate).setId(userId);
        verify(userRepository).save(userToUpdate);
    }

    @Test(expected = NotFoundException.class)
    public void testUpdateUsernameForNonExistingUser() {
        User userToUpdate = new User();
        when(userRepository.existsById(any(Long.class))).thenReturn(false);
        userController.updateUserName(1L, userToUpdate, null);
    }

    @Test
    public void testDeleteExistingUserWithNoProcess() {
        User userToDelete = mock(User.class);
        when(processUserRepository.existsByUserId(any(Long.class))).thenReturn(false);
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(userToDelete));
        doNothing().when(userRepository).delete(any(User.class));
        assertNotNull(userController.deleteUser(1L));
        verify(userRepository).delete(userToDelete);
    }

    @Test(expected = ConflictException.class)
    public void testDeleteExistingUserWithProcesses() {
        when(processUserRepository.existsByUserId(any(Long.class))).thenReturn(true);
        userController.deleteUser(1L);
    }

    @Test(expected = NotFoundException.class)
    public void testDeleteNonExistingUser() {
        when(processUserRepository.existsByUserId(any(Long.class))).thenReturn(false);
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.empty());
        userController.deleteUser(1L);
    }

}
