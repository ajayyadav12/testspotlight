package com.ge.finance.spotlight.controllers;

import com.ge.finance.spotlight.exceptions.ConflictException;
import com.ge.finance.spotlight.exceptions.NotFoundException;
import com.ge.finance.spotlight.models.Permission;
import com.ge.finance.spotlight.models.System;
import com.ge.finance.spotlight.models.User;
import com.ge.finance.spotlight.models.UserPermission;
import com.ge.finance.spotlight.repositories.ProcessUserRepository;
import com.ge.finance.spotlight.repositories.UserPermissionRepository;
import com.ge.finance.spotlight.repositories.UserRepository;
import com.ge.finance.spotlight.requests.UserPermissionRequest;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/users")
public class UserController {

    private UserRepository userRepository;
    private ProcessUserRepository processUserRepository;
    private UserPermissionRepository userPermissionRepository;

    List<User> userlist = null;

    public UserController(UserRepository userRepository, ProcessUserRepository processUserRepository,
            UserPermissionRepository userPermissionRepository) {
        this.userRepository = userRepository;
        this.processUserRepository = processUserRepository;
        this.userPermissionRepository = userPermissionRepository;
    }

    @GetMapping("/")
    List<User> index(@RequestParam Map<String, String> filters) {
        boolean mobileUsersOnly = Boolean.parseBoolean(filters.get("mobileUsersOnly"));
        if (!mobileUsersOnly) {
            return userRepository.findAll();
        } else {
            return userRepository.findByPhoneNumberNotNullAndCarrierNotNullOrderByName();
        }
    }

    @PostMapping("/")
    @PreAuthorize("hasAuthority('admin')")
    User create(@RequestBody User user) {
        return userRepository.save(user);
    }

    @GetMapping("/{userId}")
    User getUser(@PathVariable(name = "userId") Long userId, Authentication authentication) {
        return userRepository.findById(userId).orElseThrow(NotFoundException::new);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAuthority('admin')")
    User updateUserName(@PathVariable(name = "userId") Long userId, @RequestBody User user,
            Authentication authentication) {
        if (userRepository.existsById(userId)) {
            user.setId(userId);
            return userRepository.save(user);
        } else {
            throw new NotFoundException();
        }
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('admin')")
    User deleteUser(@PathVariable(name = "userId") Long userId) {
        if (!processUserRepository.existsByUserId(userId)) {
            User user = userRepository.findById(userId).orElseThrow(NotFoundException::new);
            userRepository.delete(user);
            return user;
        } else {
            throw new ConflictException("User is assigned to at least one process");
        }
    }

    @GetMapping("/{userId}/permissions")
    List<?> bySubmitPermission(Authentication authentication, @PathVariable(name = "userId") Long userId) {
        List<?> userPermissions = userPermissionRepository.findByUserId(userId);
        return userPermissions;
    }

    @PostMapping("/{userId}/permission")
    @PreAuthorize("hasAuthority('admin')")
    UserPermission createUserPermission(@PathVariable(name = "userId") Long userId,
            @RequestBody UserPermissionRequest userPermissionRequ) {
        User user = userRepository.findById(userId).orElseThrow(NotFoundException::new);
        UserPermission userPermissionSub;
        UserPermission userPermissionView = new UserPermission();

        if (userPermissionRequ.isUpload()) {
            addPermissionUser (userPermissionRequ, user, "submit");
            userPermissionView = addPermissionUser (userPermissionRequ, user, "view");
        }

        if (userPermissionRequ.isView()) {
            userPermissionView = addPermissionUser (userPermissionRequ, user, "view");
        }

        return userPermissionView;
    }

    UserPermission addPermissionUser (UserPermissionRequest userPerRequ, User user, String type) {
        UserPermission userPermissionGrant = new UserPermission();
        Permission uploadPermission;

        if (type == "view"){
            uploadPermission = new Permission();
            uploadPermission.setId(new Long(1));
            uploadPermission.setPermission(type);
        } else {
            uploadPermission = new Permission();
            uploadPermission.setId(new Long(2));
            uploadPermission.setPermission(type);
        }

        UserPermission userPermission = 
            userPermissionRepository.findByUserIdAndReceiverIdAndSenderIdAndPermission(user.getId(), 
            userPerRequ.getReceiver().getId(), 
            userPerRequ.getSender().getId(), 
            uploadPermission);
       
        if (userPermission == null) {
            userPermissionGrant = new UserPermission();
            userPermissionGrant.setPermission(uploadPermission);
            userPermissionGrant.setUser(user);
            userPermissionGrant.setReceiver(userPerRequ.getReceiver());
            userPermissionGrant.setSender(userPerRequ.getSender());
            userPermissionRepository.save(userPermissionGrant);    
        }

        return userPermissionGrant;

    }

    @DeleteMapping("/{userId}/permission/{userPermissionId}")
    @PreAuthorize("hasAuthority('admin')")
    void removeUserPermission(@PathVariable(name = "userId") Long userId,@PathVariable(name = "userPermissionId") Long userPermissionId) {        

        UserPermission permission = userPermissionRepository.findById(userPermissionId).orElseThrow(NotFoundException::new);
        List<UserPermission> permissions = userPermissionRepository.findByUserIdAndReceiverIdAndSenderId(userId, permission.getReceiver().getId(), permission.getSender().getId());

        for (UserPermission userPermission : permissions) {            
            userPermissionRepository.delete(userPermission);    
        }    

    }

}
