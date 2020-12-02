package com.ge.finance.spotlight.controllers;

import com.ge.finance.spotlight.exceptions.ConflictException;
import com.ge.finance.spotlight.exceptions.NotFoundException;
import com.ge.finance.spotlight.models.User;
import com.ge.finance.spotlight.repositories.ProcessUserRepository;
import com.ge.finance.spotlight.repositories.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/users")
public class UserController {

    private UserRepository userRepository;
    private ProcessUserRepository processUserRepository;

    public UserController(UserRepository userRepository, ProcessUserRepository processUserRepository) {
        this.userRepository = userRepository;
        this.processUserRepository = processUserRepository;
    }

    @GetMapping("/")
    List<User> index() {
        return userRepository.findAll();
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
        if (processUserRepository.countByUserId(userId) == 0) {
            User user = userRepository.findById(userId).orElseThrow(NotFoundException::new);
            userRepository.delete(user);
            return user;
        } else {
            throw new ConflictException();
        }
    }
}
