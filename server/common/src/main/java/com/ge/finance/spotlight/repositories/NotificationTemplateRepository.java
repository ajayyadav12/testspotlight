package com.ge.finance.spotlight.repositories;

import java.util.List;

import com.ge.finance.spotlight.models.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    @Override
    @Query
    List<NotificationTemplate> findAll();
}
