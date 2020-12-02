package com.ge.finance.spotlight.controllers;

import com.ge.finance.spotlight.models.ProcessType;
import com.ge.finance.spotlight.repositories.ProcessTypeRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/process-types")
public class ProcessTypeController {

    private ProcessTypeRepository processTypeRepository;

    public ProcessTypeController(ProcessTypeRepository processTypeRepository) {
        this.processTypeRepository = processTypeRepository;
    }

    @GetMapping("/")
    List<ProcessType> index() {
        return processTypeRepository.findAll();
    }

}
