package com.ge.finance.spotlight.controllers;

import com.ge.finance.spotlight.exceptions.NotFoundException;
import com.ge.finance.spotlight.models.NotificationTemplate;
import com.ge.finance.spotlight.repositories.NotificationTemplateRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/notification-templates")
public class NotificationTemplateController {

    private NotificationTemplateRepository notificationTemplateRepository;    

    public NotificationTemplateController(NotificationTemplateRepository notificationTemplateRepository) {
        this.notificationTemplateRepository = notificationTemplateRepository;        
    }

    @GetMapping("/")
    List<NotificationTemplate> index() {
        return notificationTemplateRepository.findAll();
    }

    @PostMapping("/")
    @PreAuthorize("hasAuthority('admin')")
    NotificationTemplate create(@RequestBody NotificationTemplate notificationTemplate) {
        return notificationTemplateRepository.save(notificationTemplate);
    }

    @GetMapping("/{notificationTemplateId}")
    NotificationTemplate get(@PathVariable(name="notificationTemplateId") Long notificationTemplateId) {
        return notificationTemplateRepository.findById(notificationTemplateId).orElseThrow(NotFoundException::new);
    }

    @PutMapping("/{notificationTemplateId}")
    @PreAuthorize("hasAuthority('admin')")
    NotificationTemplate update(@PathVariable(name="notificationTemplateId") Long notificationTemplateId, @RequestBody NotificationTemplate notificationTemplate) {
        if (notificationTemplateRepository.existsById(notificationTemplateId)) {
            notificationTemplate.setId(notificationTemplateId);
            return notificationTemplateRepository.save(notificationTemplate);
        } else {
            throw new NotFoundException();
        }
    }

    @DeleteMapping("/{notificationTemplateId}")
    @PreAuthorize("hasAuthority('admin')")
    NotificationTemplate delete(@PathVariable(name="notificationTemplateId") Long notificationTemplateId) {
        NotificationTemplate notificationTemplate = notificationTemplateRepository.findById(notificationTemplateId).orElseThrow(NotFoundException::new);
        notificationTemplateRepository.deleteById(notificationTemplateId);
            return notificationTemplate;
    }

}
