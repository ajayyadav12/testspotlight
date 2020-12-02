package com.ge.finance.spotlight.controllers;

import com.ge.finance.spotlight.models.ClosePhase;
import com.ge.finance.spotlight.repositories.ClosePhaseRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/close-phases")
public class ClosePhaseController {

    private ClosePhaseRepository closePhaseRepository;

    public ClosePhaseController(ClosePhaseRepository closePhaseRepository) {
        this.closePhaseRepository = closePhaseRepository;
    }

    @GetMapping("/")
    List<ClosePhase> index() {
        return closePhaseRepository.findAll();
    }

}
