package com.ge.finance.spotlight.repositories;

import java.util.List;

import com.ge.finance.spotlight.models.ScheduleReport;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleReportRepository extends JpaRepository<ScheduleReport, Long> {

    List<ScheduleReport> findByUserId(Long userId);
}