package com.ge.finance.spotlight.controllers;

import com.ge.finance.spotlight.exceptions.ConflictException;
import com.ge.finance.spotlight.exceptions.NotFoundException;
import com.ge.finance.spotlight.models.Sender;
import com.ge.finance.spotlight.repositories.ProcessRepository;
import com.ge.finance.spotlight.repositories.SenderRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/senders")
public class SenderController {

    private SenderRepository senderRepository;
    private ProcessRepository processRepository;    

    @Autowired
    public SenderController(SenderRepository senderRepository, ProcessRepository processRepository) {        
        this.senderRepository = senderRepository;
        this.processRepository = processRepository;        
    }    

    @GetMapping("/")
    List<Sender> index() {
        return senderRepository.findAll();
    }

    @PostMapping("/")
    @PreAuthorize("hasAuthority('admin')")
    Sender create(@RequestBody Sender sender) {   
        return senderRepository.save(sender);      
    }

    @GetMapping("/{senderId}")
    Sender get(@PathVariable(name="senderId") Long senderId) {
        return senderRepository.findById(senderId).orElseThrow(NotFoundException::new);
    }

    @PutMapping("/{senderId}")
    @PreAuthorize("hasAuthority('admin')")
    Sender update(@PathVariable(name="senderId") Long senderId, @RequestBody Sender sender) {
        if (senderRepository.existsById(senderId)) {
            sender.setId(senderId);
            return senderRepository.save(sender);
        } else {
            throw new NotFoundException();
        }
    }

    @DeleteMapping("/{senderId}")
    @PreAuthorize("hasAuthority('admin')")
    Sender delete(@PathVariable(name="senderId") Long senderId) {
        if (processRepository.countBySenderId(senderId) == 0) {
            Sender sender = senderRepository.findById(senderId).orElseThrow(NotFoundException::new);
            senderRepository.delete(sender);
            return sender;
        } else {
            throw new ConflictException();
        }
    }

}
