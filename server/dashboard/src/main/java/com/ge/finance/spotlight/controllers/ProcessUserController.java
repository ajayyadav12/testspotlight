package com.ge.finance.spotlight.controllers;

import com.ge.finance.spotlight.models.ProcessUser;
import com.ge.finance.spotlight.repositories.ProcessUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/my-processes")
public class ProcessUserController {

    private ProcessUserRepository processUserRepository;

    public ProcessUserController(ProcessUserRepository processUserRepository) {
        this.processUserRepository = processUserRepository;
    }

    @GetMapping("/")
    Collection<Long> myProcesses(Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        return processUserRepository.findByUserSSO(sso).stream().map(ProcessUser::getProcessId).collect(Collectors.toList());
    }

}
