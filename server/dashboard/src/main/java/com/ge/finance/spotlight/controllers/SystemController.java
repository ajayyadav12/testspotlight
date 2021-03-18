package com.ge.finance.spotlight.controllers;

import com.ge.finance.spotlight.exceptions.ConflictException;
import com.ge.finance.spotlight.exceptions.NotFoundException;
import com.ge.finance.spotlight.models.System;
import com.ge.finance.spotlight.repositories.ProcessRepository;
import com.ge.finance.spotlight.repositories.SystemRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/systems")
public class SystemController {

    private SystemRepository systemRepository;
    private ProcessRepository processRepository;    

    @Autowired
    public SystemController(SystemRepository systemRepository, ProcessRepository processRepository) {        
        this.systemRepository = systemRepository;
        this.processRepository = processRepository;        
    }    

    @GetMapping("/")
    List<System> index() {
        return systemRepository.findAll();
    }

    @PostMapping("/")
    @PreAuthorize("hasAuthority('admin')")
    System create(@RequestBody System system) {   
        return systemRepository.save(system);      
    }

    @GetMapping("/{systemId}")
    System get(@PathVariable(name="systemId") Long systemId) {
        return systemRepository.findById(systemId).orElseThrow(NotFoundException::new);
    }

    @GetMapping("/close-phase/{closePhaseId}")
    List<System> getByClosePhaseId(@PathVariable(name="closePhaseId") Long closePhaseId) {
        return systemRepository.findByClosePhaseIdOrderByNameAsc(closePhaseId);
    }

    @GetMapping("/relationships/{systemId}")
    List<?> getRelationships(@PathVariable(name="systemId") Integer systemId, @RequestParam Map<String, String> filters) {
        Integer timeOption = Integer.parseInt(filters.get("timeOption"));            
        return systemRepository.findSystemRelationships(systemId, timeOption);
    }

    @PutMapping("/{systemId}")
    @PreAuthorize("hasAuthority('admin')")
    System update(@PathVariable(name="systemId") Long systemId, @RequestBody System system) {
        if (systemRepository.existsById(systemId)) {
            system.setId(systemId);
            return systemRepository.save(system);
        } else {
            throw new NotFoundException();
        }
    }

    @DeleteMapping("/{systemId}")
    @PreAuthorize("hasAuthority('admin')")
    System delete(@PathVariable(name="systemId") Long systemId) {
        if (!processRepository.existsBySenderIdOrReceiverId(systemId, systemId)) {
            System system = systemRepository.findById(systemId).orElseThrow(NotFoundException::new);
            systemRepository.delete(system);
            return system;
        } else {
            throw new ConflictException("System is being used as Sender or Receiver in at least one process");            
        }
    }

}
