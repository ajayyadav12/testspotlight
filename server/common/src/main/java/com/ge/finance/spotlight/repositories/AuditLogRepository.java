package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.AuditLog;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

}