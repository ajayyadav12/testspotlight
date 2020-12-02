package com.ge.finance.spotlight.controllers;

import com.ge.finance.spotlight.models.Status;
import com.ge.finance.spotlight.repositories.StatusRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/status")
public class StatusController {

    private StatusRepository statusRepository;

    public StatusController(StatusRepository statusRepository) {
        this.statusRepository = statusRepository;
    }

    @GetMapping("/")
    List<Status> index() {
        return statusRepository.findAll();
    }

}
