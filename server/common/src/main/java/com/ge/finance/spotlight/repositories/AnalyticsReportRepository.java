package com.ge.finance.spotlight.repositories;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import com.ge.finance.spotlight.models.AnalyticsReport;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalyticsReportRepository extends JpaRepository<AnalyticsReport, Long> {

    List<AnalyticsReport> findByStartTimeIsBetweenOrderByStartTimeAsc(Date start, Date end);

    @Transactional
    void deleteByscheduleReportId(Long scheduleDefinitionId);
}