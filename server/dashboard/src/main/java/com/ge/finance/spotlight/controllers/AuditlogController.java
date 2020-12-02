
package com.ge.finance.spotlight.controllers;

import java.util.Date;

import com.ge.finance.spotlight.models.AuditLog;
import com.ge.finance.spotlight.models.User;
import com.ge.finance.spotlight.repositories.AuditLogRepository;
import com.ge.finance.spotlight.repositories.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auditlog")
public class AuditlogController {

    private AuditLogRepository auditLogRepository;
    private UserRepository userRepository;

    public AuditlogController(AuditLogRepository auditLogRepository, UserRepository userRepository) {

        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/{moduleName}")
    AuditLog create(@PathVariable(name = "moduleName") String moduleName, Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        User user = userRepository.findFirstBySso(sso);
        AuditLog auditLog = new AuditLog();
        auditLog.setModule(moduleName);
        auditLog.setUser(user);
        auditLog.setLogInTime(new Date());
        return auditLogRepository.save(auditLog);
    }

}
