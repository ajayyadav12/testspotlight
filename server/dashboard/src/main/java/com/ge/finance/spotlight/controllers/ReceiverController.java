package com.ge.finance.spotlight.controllers;

import com.ge.finance.spotlight.exceptions.ConflictException;
import com.ge.finance.spotlight.exceptions.NotFoundException;
import com.ge.finance.spotlight.models.Receiver;
import com.ge.finance.spotlight.repositories.ProcessRepository;
import com.ge.finance.spotlight.repositories.ReceiverRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/receivers")
public class ReceiverController {

    private ReceiverRepository receiverRepository;
    private ProcessRepository processRepository;    

    @Autowired
    public ReceiverController(ReceiverRepository receiverRepository, ProcessRepository processRepository) {
        this.receiverRepository = receiverRepository;
        this.processRepository = processRepository;        
    }
    

    @GetMapping("/")
    List<Receiver> index() {            
        return receiverRepository.findAll();
    }

    @PostMapping("/")
    @PreAuthorize("hasAuthority('admin')")
    Receiver create(@RequestBody @Valid Receiver receiver) {
        return receiverRepository.save(receiver);          
    }

    @GetMapping("/{receiverId}")
    Receiver get(@PathVariable(value="receiverId") Long receiverId) {
        return receiverRepository.findById(receiverId).orElseThrow(NotFoundException::new);
    }

    @PutMapping("/{receiverId}")
    @PreAuthorize("hasAuthority('admin')")
    Receiver update(@PathVariable(value="receiverId") Long receiverId, @RequestBody @Valid Receiver receiver) {
        if (receiverRepository.existsById(receiverId)) {
            receiver.setId(receiverId);
            return receiverRepository.save(receiver);
        } else {
            throw new NotFoundException();
        }
    }

    @DeleteMapping("/{receiverId}")
    @PreAuthorize("hasAuthority('admin')")
    Receiver delete(@PathVariable(value="receiverId") Long receiverId) {
        if (processRepository.countByReceiverId(receiverId) == 0) {
            Receiver receiver = receiverRepository.findById(receiverId).orElseThrow(NotFoundException::new);
            receiverRepository.delete(receiver);
            return receiver;
        } else {
            throw new ConflictException();
        }
    }

    
}
