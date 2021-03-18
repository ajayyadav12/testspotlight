package com.ge.finance.spotlight.repositories;

import java.util.Date;
import java.util.List;

import com.ge.finance.spotlight.models.UserNotfication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

public interface UserNotificationRepository extends CrudRepository<UserNotfication, Long>,
                JpaRepository<UserNotfication, Long>, JpaSpecificationExecutor<UserNotfication> {

        UserNotfication findBySsoAndScheduleId(long sso, long scheduleId);

        List<UserNotfication> findAllBySsoAndStatusAndStartTimeGreaterThanOrderByStartTimeDesc(long sso, String status,
                        Date date);

        boolean existsBySsoAndScheduleId(long sso, long scheduleId);

        boolean existsBySsoAndProcessType(long sso, String processType);

        boolean existsBySsoAndProcessTypeAndStatus(long sso, String processType, String status);

        UserNotfication findBySsoAndProcessTypeAndStatus(long sso, String processType, String status);

}
