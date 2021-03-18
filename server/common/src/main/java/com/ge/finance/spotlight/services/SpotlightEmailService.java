package com.ge.finance.spotlight.services;

import java.util.Map;

import com.ge.finance.spotlight.models.*;
import com.ge.finance.spotlight.models.Process;
import com.ge.finance.spotlight.dto.EmailModel;

public interface SpotlightEmailService {

        public Map<String, String> sendProcessSummaryReport(Long notificationTemplateId,
                        AnalyticsReport analyticsReport, String to);

        public void genericSend(EmailModel emailModel);

        void sendProcessExportDecision(Long notificationTemplateId, String to, Process process, User user, ProcessExportRequest processExportRequest);

}
